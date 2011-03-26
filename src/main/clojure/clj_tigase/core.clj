(ns clj-tigase.core
  (:use [clojure.string :only (trim)])
  (:import javax.xml.namespace.QName
           tigase.xml.Element
           tigase.server.Packet)
  )

(declare to-tigase-element)

(defn packet?
  "Returns if the element is a packet"
  [element]
  (instance? Packet element))

(defn element?
  "Returns if the argument is an element"
  [arg]
  (instance? Element arg))

(defn parse-qname
  [^QName qname]
  {:name (.getLocalPart qname)
   :prefix (.getPrefix qname)})

(defn ns-prefix
  [k]
  (apply str
         "xmlns"
         (if (not= k "")
           (list ":" k))))

(defn element-name
  [name prefix]
  (str (if (not= prefix "")
         (str prefix ":"))
       name))

(defn make-element-qname
  [{:keys [name prefix]}]
  (Element. (element-name name prefix)))

(defn get-qname
  "Returns a map representing the QName of the given element"
  [element]
  (parse-qname (.getQName element)))

(defn make-element-qname
  [{:keys [name prefix]}]
  (Element. (element-name name prefix)))

(defn assign-namespace
  [^Element element
   namespace-map
   [k v]]
  (if (not= (get namespace-map k) v)
    (do (.addAttribute
         element (ns-prefix k) v)
        [k v])))

(declare make-element)

(defn process-child
  "adds content of the appropriate type to the element"
  [^Element element item]
  #_(println "item: " item)
  (if (element? item)
    (.addChild element item)
    (if (map? item)
      (.addChild element (to-tigase-element item))
      (if (vector? item)
        (if (seq item)
          (.addChild element (apply make-element item)))
        (if (string? item)
          (.setCData element (trim item))
          (if (coll? item)
            (doseq [i item]
              (process-child element i))))))))

(defn ^Element make-element
  "Create a tigase element"
  ([spec]
     (apply make-element spec))
  ([name attrs]
     (make-element name attrs nil))
  ([name attrs & children]
     (let [element (Element. name)]
       (doseq [[attr val] attrs]
         (.addAttribute element attr (str val)))
       (doseq [child children]
         (process-child element child))
       element)))

(defn to-tigase-element
  "turns a map into a tigase element"
  [{:keys [tag attrs content]}]
  (let [attribute-names (into-array String (map name (keys attrs)))
        attribute-values (into-array String (vals attrs))
        tag-name (name tag)
        element (Element. tag-name attribute-names attribute-values)]
    (doseq [item content]
      (process-child element item))
    element))

(defn children
  "returns the child elements of the given element"
  ([^Element element]
     (if element
       (seq (.getChildren element))))
  ([^Packet packet path]
     (if packet
       (seq (.getElemChildren packet path)))))

(defn merge-namespaces
  [^Element element
   namespace-map
   namespaces]
  (merge namespace-map
         (into {}
               (map
                (partial assign-namespace element namespace-map)
                namespaces))))

;; (defn add-children
;;   [^Element element abdera-element bound-namespaces]
;;   (doseq [child-element (.getElements abdera-element)]
;;     (.addChild element
;;                (abdera-to-tigase-element
;;                 child-element bound-namespaces))))

(defn add-attributes
  [^Element element abdera-element]
  (doseq [attribute (.getAttributes abdera-element)]
    (let [value (.getAttributeValue abdera-element attribute)]
      (.addAttribute element (.getLocalPart attribute) value))))

(defn make-packet
  [{:keys [to from body type id] :as packet-map}]
  (let [element-name (condp = type
                         :result "iq"
                         :set "iq"
                         :get "iq"
                         :chat "message"
                         :headline "message")
        element (make-element
                 [element-name {"id" id
                                 "type" (if type (name type) "")
                                 "to" to
                                 "from" from}])]
    (if body (.addChild element body))
    (Packet/packetInstance element from to)))
