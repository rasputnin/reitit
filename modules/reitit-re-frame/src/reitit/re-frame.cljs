(ns reitit.re-frame
  (:require [re-frame.core :as re]
            [reitit.frontend :as reitit-frontend]))

(re/reg-event-db ::navigate
  (fn [db [_ match]]
    (assoc db ::routes (assoc (::routes db)
                              :match match
                              :controllers (reitit-frontend/apply-controllers (:controllers (::routes db)) match)))))

(re/reg-event-db :routes/init
  (fn [db [_ router options]]
    (when (::routes db)
      (reitit-frontend/stop! (::routes db)))

    (assoc db ::routes (assoc (reitit-frontend/start! router #(re/dispatch [::navigate %]) options)
                              :match nil
                              :controllers []))))

;;
;; Internal subscriptions
;;

(re/reg-sub ::state
  (fn [db]
    (::routes db)))

(re/reg-sub ::router
  :<- [::state]
  (fn [state]
    (:router state)))

;;
;; Public subscriptions
;;

(re/reg-sub :routes/match
  :<- [::state]
  (fn [state]
    (:match state)))

(re/reg-sub :routes/data
  :<- [:routes/match]
  (fn [match _]
    (:data match)))

(re/reg-sub :routes/match-by-name
  :<- [::router]
  (fn [router [_ k params]]
    (reitit-frontend/match-by-name router k params)))

;; FIXME: re-run if anything changes, because needs history for path-prefix etc.?
(re/reg-sub :routes/href
  :<- [::state]
  (fn [state [_ k params]]
    (if state
      (reitit-frontend/href state k params))))

(defn routed-view
  "Renders the current view component.

  Current view component is the :view from last :controllers entry with :view set."
  []
  (let [{:keys [data params] :as match} @(re/subscribe [:routes/match])
        {:keys [name view]} data]
    (if view
      ;; NOTE: View component is passed the complete params map, not just the controllers params
      ;; :routes/match doesn't know about controller params
      [view params]
      [:div "No view component defined for route: " name])))

(defn href
  "Returns href (including #) for given route name and parameters."
  ([name] (href name nil))
  ([name params]
   @(re/subscribe [:routes/href name params])))

(re/reg-fx :update-uri
  (fn [[k params]]
    (if k
      (let [this @(re/subscribe [::state])]
        (reitit-frontend/replace-token this k params)))))

(re/reg-event-fx :routes/update-uri
  (fn [_ [_ & v]]
    {:update-uri v}))
