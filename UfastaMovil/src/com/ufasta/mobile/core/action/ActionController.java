package com.ufasta.mobile.core.action;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.ufasta.mobile.core.logger.Logger;
import com.ufasta.mobile.core.request.Params;

public class ActionController {

	private class Saver {

		public State state = null;
		public Params bundle = null;

	}

	private Map<IAction, Queue<Saver>> storedActions = new Hashtable<ActionController.IAction, Queue<Saver>>();

	private void storeAction(IAction action, State newStatus, Params bundle) {
		Saver s = new Saver();
		s.state = newStatus;
		if (bundle != null) {
			s.bundle = new Params(bundle);
		}

		Queue<Saver> queue = storedActions.get(action);
		if (queue == null) {
			queue = new ConcurrentLinkedQueue<ActionController.Saver>();
			storedActions.put(action, queue);
		}

		queue.add(s);
	}

	/**
	 * Mandatory interface that enumerations containing actions must implement.
	 */
	public interface IAction {

		/**
		 * @return The internal number used to handled the particular mAction
		 */
		public int getNumber();

		/**
		 * @return The name for the constant used to identify the particular
		 *         mAction
		 */
		public String getName();
	}

	/** Handle mState for the STATUS of the operation being performed */
	public enum State {
		STATE_REMOVE, STATE_INPROGRESS, STATE_DONE, STATE_ERROR, STATE_EXCEPTION
	}

	/** Containers to save */
	private Hashtable<IAction, State> monitoredActions = new Hashtable<IAction, State>();
	private Hashtable<IAction, Params> monitoredActionBundles = new Hashtable<IAction, Params>();

	/** Listeners to operation status changes, using operation ID as key */
	private Map<IAction, Vector<IActionListener>> actionListeners = new LinkedHashMap<IAction, Vector<IActionListener>>();

	/**
	 * Default key to identify errors
	 */
	public static final String KEY_ERROR_NUMBER = "key_error_number";
	public static final String KEY_ERROR_TITLE = "key_error_title";
	public static final String KEY_ERROR_DESCRIPTION = "key_error_description";

	/**
	 * Error codes
	 */
	public static final String ERROR_INVALID_TOKEN = "2001";
	public static final String ERROR_CONNECTION = "2002";

	/**
	 * Set the status of the mAction being monitored. For the situation where
	 * the mState is -1, then the operation is not monitored anymore
	 */
	public void setActionStatus(IAction action, State newStatus) {
		setActionStatus(action, newStatus, null);
	}

	/**
	 * Set the status of the mAction being monitored. For the situation where
	 * the mState is -1 (What? [VSM]), then the operation is not monitored
	 * anymore
	 */
	public void setActionStatus(IAction action, State newStatus, Params bundle) {
		if (newStatus == State.STATE_REMOVE) {
			monitoredActions.remove(action);
			monitoredActionBundles.remove(action);
		} else {
			monitoredActions.put(action, newStatus);
			if (bundle != null) {
				monitoredActionBundles.put(action, bundle);
			}

			/**
			 * Notify all the listener for the event
			 */

			Vector<IActionListener> actions;

			synchronized (actionListeners) {
				actions = actionListeners.get(action);
				actionListeners.notifyAll();
			}

			if (actions == null) {
				Logger.info(String.format(
						"No listener for action %s, storing it",
						action.getName()));
				storeAction(action, newStatus, bundle);
				return;
			}

			fireListeners(actions, action, newStatus, bundle);

		}

	}

	private void fireListeners(Vector<IActionListener> actionListeners,
			IAction action, State newStatus, Params bundle) {
		Enumeration<IActionListener> listeners = actionListeners.elements();
		while (listeners.hasMoreElements()) {
			IActionListener listener = listeners.nextElement();
			switch (newStatus) {
			case STATE_DONE:
				listener.onDone(action, bundle);
				break;

			case STATE_INPROGRESS:
				listener.onProgress(action, bundle);
				break;

			case STATE_ERROR:
				listener.onError(action, bundle);
				break;
			case STATE_EXCEPTION:
				listener.onException(action, bundle);
				break;

			case STATE_REMOVE:
				// Nothing to do, already done by ActionController [see Line
				// 74] but logged just in case.
				Logger.error(String
						.format("Remove state wasn't handled by the controller, this is not a regular behavior for state %s and action %s. ",
								newStatus.name(), action.getName()));
				break;

			default:
				Logger.error(String.format("Unhandled state %s",
						newStatus.name()));
				break;
			}
		}
	}

	/**
	 * Retrieve the status of the operation being monitored. If the operation is
	 * not being monitored at a specific point, then something is wrong.
	 * 
	 * @return The correct State or null if not found (not good, check your
	 *         status flow)
	 */
	public State getActionStatus(IAction action) {
		if (monitoredActions.containsKey(action)) {
			return monitoredActions.get(action);
		}

		Logger.warn(String.format("No status defined for %s.", action.getName()));
		// If we got here there is something wrong in the status flow
		return null;
	}

	/**
	 * Retrieves the bundle's operation status being monitored. If the operation
	 * is not being monitored at an specific point, then null is returned.
	 * 
	 * @return
	 */
	public Params getActionStatusBundle(IAction operation) {
		return monitoredActionBundles.get(operation);
	}

	/**
	 * <p>
	 * Add a new listener to desired actions.
	 * </p>
	 * <p>
	 * if actions is null, newListener will be binded to all IAction elements
	 * </p>
	 * 
	 * @param newListener
	 *            New class implementing IActionListener interface.
	 */
	public void register(IActionListener newListener, IAction... actions) {

		if (actions == null) {
			register(newListener);
			return;
		}

		synchronized (actionListeners) {

			for (IAction action : actions) {
				Vector<IActionListener> listener = actionListeners.get(action);

				if (listener == null) {
					listener = new Vector<IActionListener>();
					actionListeners.put(action, listener);
				}

				if (!listener.contains(newListener)) {
					listener.add(newListener);
				}

				Queue<Saver> stored = storedActions.get(action);
				if (stored != null) {
					while (stored.size() > 0) {
						Saver s = stored.poll();
						fireListeners(listener, action, s.state, s.bundle);
					}
					storedActions.remove(action);
				}

			}

			actionListeners.notifyAll();
		}

	}

	/**
	 * Remove an mAction listener.
	 * 
	 * @param removeListener
	 *            A class implementing IActionListener interface.
	 */
	public void unregister(IActionListener removeListener) {

		synchronized (actionListeners) {

			for (IAction action : Actions.values()) {
				Vector<IActionListener> listener = actionListeners.get(action);

				if (listener != null) {
					listener.remove(removeListener);

					if (listener.size() == 0) {
						actionListeners.remove(action);
					}

				}
			}
			actionListeners.notifyAll();
		}
	}

	public void unregister(IActionListener removeListener, IAction... actions) {

		if (actions == null) {
			unregister(removeListener);
			return;
		}

		synchronized (actionListeners) {

			for (IAction action : actions) {
				Vector<IActionListener> listener = actionListeners.get(action);

				if (listener != null) {
					listener.remove(removeListener);

					if (listener.size() == 0) {
						actionListeners.remove(action);
					}

				}
			}
			actionListeners.notifyAll();
		}
	}

	/**
	 * Clear all the objects in the container. This method shall only be invoked
	 * when there is no need to re
	 */
	public void reset() {
		/** Operations to monitor */
		monitoredActions.clear();
		monitoredActionBundles.clear();

		/** Listeners to operation status changes, using operation ID as key */
		synchronized (actionListeners) {
			actionListeners.clear();
			actionListeners.notifyAll();
		}
	}

	public void clear() {
		// Clean your stuff here
	}
}
