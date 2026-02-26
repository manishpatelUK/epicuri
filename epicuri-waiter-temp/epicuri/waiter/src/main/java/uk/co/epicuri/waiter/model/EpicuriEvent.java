package uk.co.epicuri.waiter.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EpicuriEvent implements Serializable {
//	public static final String TAG_EVENT_PERFORMED = "performed";
	
	public interface Notification {
		/**
		 * Get notification id
		 * @return id of the notification
		 */
		String getId();
		/**
		 * get the type - one of {@link Type}
		 * @return type of the notification
		 */
		Type getType() ;

		/**
		 * get the text of the notification
		 * @return user-facing text
		 */
		String getText() ;
		/**
		 * due date for the notificaiton
		 * @return due date (GMT)
		 */
		Date getDue();
		/**
		 * get target for the notification (e.g. waiter/action)
		 * @return target
		 */
		String getTarget();
		
		/**
		 * return list of acknowledgement dates,  if any
		 * @return list of acknowledgement date
		 */
		List<Date> getAcknowledgements();
	}

	private static final String TAG_DELAY = "Delay";
	private static final String TAG_INITIALDELAY = "InitialDelay";
	private static final String TAG_PERIOD = "Period";
	private static final String TAG_NOTIFICATIONS = "Notifications";
	
	private static final String TAG_NOTIFICATION_ID = "Id";
	private static final String TAG_NOTIFICATION_TEXT = "Text";
	private static final String TAG_NOTIFICATION_TYPE = "Type";
	private static final String TAG_NOTIFICATION_DUE = "Due";
	private static final String TAG_NOTIFICATION_CREATED = "Created";
	private static final String TAG_NOTIFICATION_TARGET = "Target";
	private static final String TAG_NOTIFICATION_SESSION_ID = "Session";
	private static final String TAG_NOTIFICATION_ACKS = "Acknowledgements";

//	private static final String TAG_ACKNOWLEDGEMENT_ID = "Id";
	private static final String TAG_ACKNOWLEDGEMENT_TIME = "Time";

	public enum Type {
		TYPE_ADHOC("Adhoc"),
		TYPE_SCHEDULED("Notification"),
		TYPE_RECURRING("Recurring");
		
		private final String stringDef;
		
		Type(String sd){
			stringDef = sd;
		}

		@Override
		public String toString() {
			return stringDef;
		}
		
		public static Type fromString(String typeString) {
			if(typeString != null) {
				for(Type b : Type.values()) {
					if(typeString.equalsIgnoreCase(b.stringDef)) {
						return b;
					}
				}
			}
			throw new RuntimeException("Unrecognised type: " + typeString);
		}
	}

	/**
	 * delay in milliseconds from session start
	 */
	private final long delay;

	/**
	 * list of notifications
	 */
	private List<ScheduledEventNotification> notifications;
	
	/**
	 * delay from start of session in milliseconds
	 */
	public long getDelay() {
		return delay;
	}

	public List<ScheduledEventNotification> getNotifications() {
		return notifications;
	}

	public EpicuriEvent(JSONObject eventJson) throws JSONException {
		delay = 1000L * eventJson.getInt(TAG_DELAY);
		JSONArray notificationsJson = eventJson.getJSONArray(TAG_NOTIFICATIONS);
		
		notifications = new ArrayList<EpicuriEvent.ScheduledEventNotification>(notificationsJson.length());
		for(int i=0; i<notificationsJson.length(); i++){
			notifications.add(new ScheduledEventNotification(notificationsJson.getJSONObject(i), this));
		}
	}
	

	/**
	 * a scheduled notification - has an attached 'event' which can be used to determine due date
	 * @author Pete Harris <peteh@thedistance.co.uk>
	 */
	public static class ScheduledEventNotification implements Notification {
		private final String id;
		private final String text;
		private final String target;
		private final List<Date> acknowledgements = new ArrayList<Date>();
		private final EpicuriEvent event;
		private Date due;
		/** this is set to true if this notification is not the next notification due for the session */
		private boolean isFutureAction = false;
		
		public ScheduledEventNotification(JSONObject notificationJson, EpicuriEvent event) throws JSONException {
			id = notificationJson.getString(TAG_NOTIFICATION_ID);
			text = notificationJson.getString(TAG_NOTIFICATION_TEXT);
			target = notificationJson.getString(TAG_NOTIFICATION_TARGET);
			this.event = event;

			if(notificationJson.has(TAG_NOTIFICATION_ACKS)){
				JSONArray acknowledgementsJson = notificationJson.getJSONArray(TAG_NOTIFICATION_ACKS);
				for(int i=0; i<acknowledgementsJson.length(); i++){
					acknowledgements.add(new Date(1000L * acknowledgementsJson.getJSONObject(i).getInt(TAG_ACKNOWLEDGEMENT_TIME)));
				}
			}
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public Type getType() {
			return Type.TYPE_SCHEDULED;
		}

		public boolean isFutureAction() {
			return isFutureAction;
		}

		public void setFutureAction(boolean isFutureAction) {
			this.isFutureAction = isFutureAction;
		}

		@Override
		public String getText() {
			return text;
		}
		
		@Override
		public Date getDue() {
			return due;
		}

		public void setDue(long sessionStart, long sessionLag){
			if(acknowledgements.isEmpty()){
				// yet to be acknowledged -> add session lag
				due = new Date(sessionStart + sessionLag + event.delay); // correction for server offset calculation
			} else {
				due = new Date(sessionStart + event.delay);
			}
		}

		@Override
		public String getTarget() {
			return target;
		}

		@Override
		public List<Date> getAcknowledgements() {
			return acknowledgements;
		}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScheduledEventNotification that = (ScheduledEventNotification) o;

            return id != null ? id.equals(that.id) : that.id == null;

        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

        @Override
		public String toString() {
			return "Scheduled \"" + text + "\" - " + due.toString();
		}
	}
	
	/**
	 * ad-hoc notification - created by the client app, or some other process
	 * @author Pete Harris <peteh@thedistance.co.uk>
	 */
	public static class AdhocNotification implements Notification {
		private final String id;
		private final String text;
		private final String target;
		private final Date created;
		private final List<Date> acknowledgements = new ArrayList<Date>();

		@Override
		public String getId() {
			return id;
		}

		@Override
		public Type getType() {
			return Type.TYPE_ADHOC;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public String getTarget() {
			return target;
		}

		public Date getDue() {
			return created;
		}

		@Override
		public List<Date> getAcknowledgements() {
			return acknowledgements;
		}

		public AdhocNotification(JSONObject notificationJson) throws JSONException {
			id = notificationJson.getString(TAG_NOTIFICATION_ID);
			text = notificationJson.getString(TAG_NOTIFICATION_TEXT);
			target = notificationJson.getString(TAG_NOTIFICATION_TARGET);
			if(notificationJson.has(TAG_NOTIFICATION_CREATED)){
				created = new Date(1000L * notificationJson.getInt(TAG_NOTIFICATION_CREATED));
			} else {
				created = null;
			}
			if(notificationJson.has(TAG_NOTIFICATION_ACKS)){
				JSONArray acknowledgementsJson = notificationJson.getJSONArray(TAG_NOTIFICATION_ACKS);
				for(int i=0; i<acknowledgementsJson.length(); i++){
					acknowledgements.add(new Date(1000L * acknowledgementsJson.getJSONObject(i).getInt(TAG_ACKNOWLEDGEMENT_TIME)));
				}
			}
		}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AdhocNotification that = (AdhocNotification) o;

            return id != null ? id.equals(that.id) : that.id == null;

        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

		@Override
		public String toString() {
			return "AdHoc \"" + text + "\" - " + created.toString();
		}
	}
	
	/**
	 * recurring event - has a period, and should pop up that long after the most recent acknowledgement
	 * @author Pete Harris <peteh@thedistance.co.uk>
	 */
	public static class RecurringEvent {
		private final int initialDelay;
		private final int period;
		private final ArrayList<RecurringNotification> notifications;
		
		/**
		 * delay from start of session, in milliseconds
		 */
		public long getInitialDelay() {
			return 1000L * initialDelay;
		}

		/**
		 * gap between instances, in milliseconds
		 */
		public long getPeriod() {
			return 1000L * period;
		}

		public ArrayList<RecurringNotification> getNotifications() {
			return notifications;
		}

		public RecurringEvent(JSONObject eventJson) throws JSONException{
			initialDelay = eventJson.getInt(TAG_INITIALDELAY);
			period = eventJson.getInt(TAG_PERIOD);
			JSONArray notificationsJson = eventJson.getJSONArray(TAG_NOTIFICATIONS);
			
			notifications = new ArrayList<RecurringNotification>(notificationsJson.length());
			for(int i=0; i<notificationsJson.length(); i++){
				notifications.add(new RecurringNotification(notificationsJson.getJSONObject(i)));
			}
		}
	}
	
	/**
	 * recurring notification
	 * @author Pete Harris <peteh@thedistance.co.uk>
	 */
	public static class RecurringNotification implements Notification {
		private final String id;
		private final String text;
		private final String target;
		private Date nextDue;
		private final List<Date> acknowledgements = new ArrayList<Date>();

		@Override
		public String getId() {
			return id;
		}

		@Override
		public Type getType() {
			return Type.TYPE_RECURRING;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public String getTarget() {
			return target;
		}

		@Override
		public Date getDue() {
			return nextDue;
		}
		
		public void setNextDue(Date nextDue) {
			this.nextDue = nextDue;
		}

		@Override
		public List<Date> getAcknowledgements() {
			return acknowledgements;
		}

		public RecurringNotification(JSONObject notificationJson) throws JSONException {
			id = notificationJson.getString(TAG_NOTIFICATION_ID);
			text = notificationJson.getString(TAG_NOTIFICATION_TEXT);
			target = notificationJson.getString(TAG_NOTIFICATION_TARGET);
			if(notificationJson.has(TAG_NOTIFICATION_ACKS)){
				JSONArray acknowledgementsJson = notificationJson.getJSONArray(TAG_NOTIFICATION_ACKS);
				for(int i=0; i<acknowledgementsJson.length(); i++){
					acknowledgements.add(new Date(1000L * acknowledgementsJson.getJSONObject(i).getInt(TAG_ACKNOWLEDGEMENT_TIME)));
				}
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			RecurringNotification that = (RecurringNotification) o;

			return id != null ? id.equals(that.id) : that.id == null;

		}

		@Override
		public int hashCode() {
			return id != null ? id.hashCode() : 0;
		}

		@Override
		public String toString() {
			return "Recurring \"" + text + "\" - " + nextDue.toString();
		}
	}
	
	/**
	 * notification summary - shown on hub view
	 * @author Pete Harris <peteh@thedistance.co.uk>
	 */
	public static class HubNotification implements Notification {
		private static final String TAG_NOTIFICATION_DELAY = "Delay";
		
		private final String id;
		private final String sessionId;
		private final String text;
		private final String target;
		private final Date due;
		private final List<Date> acknowledgements = new ArrayList<Date>();
		private final Type type;
		private final int delay;
		
		@Override
		public String getId() {
			return id;
		}
		
		@Override
		public Type getType() {
			return type;
		}

		public String getSessionId() {
			return sessionId;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public String getTarget() {
			return target;
		}
		
		/**
		 * return session delay
		 * @return delay in milliseconds
		 */
		public long getSessionDelay(){
			return 1000L * delay;
		}

		public Date getDue() {
			return due;
		}

		@Override
		public List<Date> getAcknowledgements() {
			return acknowledgements;
		}

		public HubNotification(JSONObject notificationJson) throws JSONException {
			id = notificationJson.getString(TAG_NOTIFICATION_ID);
			text = notificationJson.getString(TAG_NOTIFICATION_TEXT);
			target = notificationJson.getString(TAG_NOTIFICATION_TARGET);
			String typeString = notificationJson.getString(TAG_NOTIFICATION_TYPE);
			
			type = Type.fromString(typeString);
			
			if(notificationJson.has(TAG_NOTIFICATION_SESSION_ID)){
				sessionId = notificationJson.getString(TAG_NOTIFICATION_SESSION_ID);
			} else {
				sessionId = "-1";
			}
			delay = notificationJson.getInt(TAG_NOTIFICATION_DELAY);
			if(notificationJson.has(TAG_NOTIFICATION_DUE)){
				due = new Date(1000L * notificationJson.getInt(TAG_NOTIFICATION_DUE));
			} else {
				due = null;
			}
			if(notificationJson.has(TAG_NOTIFICATION_ACKS)){
				JSONArray acknowledgementsJson = notificationJson.getJSONArray(TAG_NOTIFICATION_ACKS);
				for(int i=0; i<acknowledgementsJson.length(); i++){
					acknowledgements.add(new Date(1000L * acknowledgementsJson.getJSONObject(i).getInt(TAG_ACKNOWLEDGEMENT_TIME)));
				}
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			HubNotification that = (HubNotification) o;

			if (id != null ? !id.equals(that.id) : that.id != null) return false;
			return sessionId != null ? sessionId.equals(that.sessionId) : that.sessionId == null;

		}

		@Override
		public int hashCode() {
			int result = id != null ? id.hashCode() : 0;
			result = 31 * result + (sessionId != null ? sessionId.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "Hub \"" + text + "\" - " + due.toString();
		}
	}
}