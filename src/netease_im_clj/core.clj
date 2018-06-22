(ns netease-im-clj.core
  (:require [clojure.math.numeric-tower :as math]
            [digest]
            [clj-http.client :as client]
            [clojure.data.json :as json]))


(def netease-im-app-key "")
(def netease-im-secret-key "")


(defn- rand-alphabet
  "生成随机字母"
  [len]
  (apply str
         (take len
               (repeatedly #(rand-nth "abcdefghijklmnopqrstuvwxyz")))))


(defn netease-im-setconfig!
  "设置网易云的appkey以及secretkey"
  [{:keys [app-key secret-key]}]
  (do (def netease-im-app-key app-key)
      (def netease-im-secret-key secret-key)))


(defn- getCheckSum
  "计算并获取CheckSum"
  [appSecret nonce curTime]
  (digest/sha-1 (str appSecret nonce curTime)))

(defn- getMD5
  [requestBody]
  (digest/md5 requestBody))


(defn netease-post
  "网易云的POST通用接口"
  [{:keys [post-url form-params]}]
  (let [timestamp (str (int (/ (.getTime (java.util.Date.)) 1000)))
        nonce (rand-alphabet 64)]
    (-> post-url
        (client/post {:headers {"AppKey" netease-im-app-key
                                "Nonce" nonce
                                "CurTime" timestamp
                                "CheckSum" (getCheckSum netease-im-secret-key
                                                        nonce timestamp)}

                      :content-type "application/x-www-form-urlencoded;charset=utf-8"
                      :form-params form-params})
        :body (json/read-str :key-fn keyword))))


(defn netease-user-create
  "创建网易云通信ID"
  [{:keys [accid name props icon token sign email birth mobile gender ex]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/user/create.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid)
                   (not (nil? name)) (assoc :name name)
                   (not (nil? props)) (assoc :props props)
                   (not (nil? icon)) (assoc :icon icon)
                   (not (nil? token)) (assoc :token token)
                   (not (nil? sign)) (assoc :sign sign)
                   (not (nil? email)) (assoc :email email)
                   (not (nil? birth)) (assoc :birth birth)
                   (not (nil? mobile)) (assoc :mobile mobile)
                   (not (nil? gender)) (assoc :gender gender)
                   (not (nil? ex)) (assoc :ex ex))}))


(defn netease-user-update
  "网易云通信ID更新"
  [{:keys [accid props token]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/user/update.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid)
                   (not (nil? props)) (assoc :props props)
                   (not (nil? token)) (assoc :token token))}))


(defn netease-user-refreshToken
  "更新并获取新token "
  [{:keys [accid]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/user/refreshToken.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid))}))


(defn netease-user-block
  "封禁网易云通信ID"
  [{:keys [accid needkick]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/user/block.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid)
                   (not (nil? needkick)) (assoc :needkick needkick))}))

(defn netease-user-unblock
  "解禁网易云通信ID"
  [{:keys [accid]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/user/unblock.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid))}))


(defn netease-user-updateUinfo
  "更新用户名片"
  [{:keys [accid name icon sign email birth mobile gender ex]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/user/updateUinfo.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid)
                   (not (nil? name)) (assoc :name name)
                   (not (nil? icon)) (assoc :name icon)
                   (not (nil? sign)) (assoc :name sign)
                   (not (nil? email)) (assoc :name email)
                   (not (nil? birth)) (assoc :name birth)
                   (not (nil? mobile)) (assoc :name mobile)
                   (not (nil? gender)) (assoc :name gender)
                   (not (nil? ex)) (assoc :name ex))}))


(defn netease-user-getUinfos
  "获取多个用户名片 "
  [{:keys [accids ;;vector类型
           ]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/user/updateUinfo.action"
    :form-params (cond-> {}
                   (not (nil? accids)) (assoc :accids (json/write-str accids)))}))

;; 用户关系托管

(defn netease-friend-add
  "加好友"
  [{:keys [accid faccid type msg]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/friend/add.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid)
                   (not (nil? faccid)) (assoc :faccid faccid)
                   (not (nil? type)) (assoc :type type)
                   (not (nil? msg)) (assoc :msg msg))}))

(defn netease-friend-update
  "更新好友相关信息"
  [{:keys [accid faccid alias ex]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/friend/update.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid)
                   (not (nil? faccid)) (assoc :faccid faccid)
                   (not (nil? alias)) (assoc :alias alias)
                   (not (nil? ex)) (assoc :msg ex))}))

(defn netease-friend-delete
  "更新好友相关信息"
  [{:keys [accid faccid]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/friend/delete.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid)
                   (not (nil? faccid)) (assoc :faccid faccid))}))

(defn netease-friend-get
  "获取好友关系"
  [{:keys [accid updatetime createtime]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/friend/get.action"
    :form-params (cond-> {}
                   (not (nil? accid)) (assoc :accid accid)
                   (not (nil? updatetime)) (assoc :updatetime updatetime)
                   (not (nil? createtime)) (assoc :createtime createtime))}))


(defn netease-sendAttachMsg
  "发送系统消息给某位用户"
  [{:keys [to attach
           pushcontent                  ;标题
           payload sound save option]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/msg/sendAttachMsg.action"
    :form-params (cond-> {:from "1" :msgtype 0}
                   (not (nil? to)) (assoc :to to)
                   (not (nil? attach)) (assoc :attach (json/write-str attach))
                   (not (nil? pushcontent)) (assoc :pushcontent pushcontent)
                   (not (nil? payload)) (assoc :payload (json/write-str payload))
                   (not (nil? sound)) (assoc :sound sound)
                   (not (nil? save)) (assoc :save save)
                   (not (nil? option)) (assoc :option (json/write-str option)))}))


(defn netease-sendBatchAttachMsg
  "群发系统消息给某些用户"
  [{:keys [toAccids attach pushcontent payload sound save option]}]
  (netease-post
   {:post-url "https://api.netease.im/nimserver/msg/sendBatchAttachMsg.action"
    :form-params (cond-> {:fromAccid "1"}
                   (not (nil? toAccids)) (assoc :toAccids (json/write-str toAccids))
                   (not (nil? attach)) (assoc :attach (json/write-str attach))
                   (not (nil? pushcontent)) (assoc :pushcontent pushcontent)
                   (not (nil? payload)) (assoc :payload (json/write-str payload))
                   (not (nil? sound)) (assoc :sound sound)
                   (not (nil? save)) (assoc :save save)
                   (not (nil? option)) (assoc :option (json/write-str option)))}))
