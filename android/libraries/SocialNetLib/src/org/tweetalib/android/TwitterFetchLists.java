/*
 * Copyright (C) 2013 Chris Lacy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tweetalib.android;

import java.util.HashMap;

import org.asynctasktex.AsyncTaskEx;

import org.tweetalib.android.model.TwitterLists;

import org.twitter4j.ResponseList;
import org.twitter4j.Twitter;
import org.twitter4j.TwitterException;
import org.twitter4j.UserList;

public class TwitterFetchLists {

	private FetchListsWorkerCallbacks mCallbacks;
	private HashMap<Integer, TwitterLists> mListsHashMap;
	private Integer mFetchListsCallbackHandle;
	private HashMap<Integer, FinishedCallback> mFinishedCallbackMap;

	/*
	 * 
	 */
	public void clearCallbacks() {
		mFinishedCallbackMap.clear();
	}
	
	/*
	 * 
	 */
	public interface FetchListsWorkerCallbacks {
		
		public Twitter getTwitterInstance();
	}
	
	/*
	 * 
	 */
	public interface FinishedCallbackInterface {
		
		public void finished(boolean successful, TwitterLists lists);
		
	}
	
	/*
	 * 
	 */
	public abstract class FinishedCallback implements FinishedCallbackInterface {
		
		static final int kInvalidHandle = -1; 
		
		public FinishedCallback() {
			mHandle = kInvalidHandle;
		}
		
		void setHandle(int handle) {
			mHandle = handle;
		}
		
		private int mHandle;
	}
	
	/*
	 * 
	 */
	public TwitterFetchLists() {
		mFinishedCallbackMap = new HashMap<Integer, FinishedCallback>();
		mFetchListsCallbackHandle = 0;
		mListsHashMap = new HashMap<Integer, TwitterLists>();

	}
	
	/*
	 * 
	 */
	public void setWorkerCallbacks(FetchListsWorkerCallbacks callbacks) {
		mCallbacks = callbacks;
	}
	
	/*
	 * 
	 */
	FinishedCallback getFetchStatusesCallback(Integer callbackHandle) {
		FinishedCallback callback = mFinishedCallbackMap.get(callbackHandle);
		return callback;
	}
	
	/*
	 * 
	 */
	void removeFetchStatusesCallback(FinishedCallback callback) {
		if (mFinishedCallbackMap.containsValue(callback)) {
			mFinishedCallbackMap.remove(callback.mHandle);
		}
	}
	
	/*
	 * 
	 */
	Twitter getTwitterInstance() {
		return mCallbacks.getTwitterInstance();
	}
	
	/*
	 * 
	 */
	public TwitterLists getLists(Integer userId, FinishedCallback callback) {
		
		TwitterLists lists = mListsHashMap.get(userId);
		
		if (callback != null) {
			trigger(userId, callback);
		}
			
		return lists;
	}
	
	public TwitterLists getLists(String screenName, FinishedCallback callback) {
		
		if (callback != null) {
			trigger(screenName, callback);
		}
			
		return null;
	}
	
	
	/*
	 * 
	 */
	private void trigger(Integer userId, FinishedCallback callback) {
		
		assert(mFinishedCallbackMap.containsValue(callback) == false);
		
		mFinishedCallbackMap.put(mFetchListsCallbackHandle, callback);
		new FetchListsTask().execute(AsyncTaskEx.PRIORITY_MEDIUM, "Fetch Lists", new FetchListsTaskInput(userId, mFetchListsCallbackHandle));
		
		mFetchListsCallbackHandle += 1;
	}
	
	private void trigger(String screenName, FinishedCallback callback) {
		
		assert(mFinishedCallbackMap.containsValue(callback) == false);
		
		mFinishedCallbackMap.put(mFetchListsCallbackHandle, callback);
		new FetchListsTask().execute(AsyncTaskEx.PRIORITY_MEDIUM, "Fetch Lists", new FetchListsTaskInput(screenName, mFetchListsCallbackHandle));
		
		mFetchListsCallbackHandle += 1;
	}
	
	/*
	 * 
	 */
	public void cancel(FinishedCallback callback) {
		
		removeFetchStatusesCallback(callback);
	}
	
	
	
	/*
	 * 
	 */
	class FetchListsTaskInput {
		
		FetchListsTaskInput(Integer userId, Integer callbackHandle) {
			mCallbackHandle = callbackHandle;
			mUserId = userId;
		}
		
		FetchListsTaskInput(String screenName, Integer callbackHandle) {
			mCallbackHandle = callbackHandle;
			mScreenName = screenName;
		}
		
		Integer mCallbackHandle;
		Integer mUserId;
		String mScreenName;
	}
	
	/*
	 * 
	 */
	class FetchListsTaskOutput {
		
		FetchListsTaskOutput(Integer callbackHandle, TwitterLists lists) {
			mCallbackHandle = callbackHandle;
			mLists = lists;
		}
		
		Integer mCallbackHandle;
		TwitterLists mLists;
	}
	
	/*
	 * 
	 */
	class FetchListsTask extends AsyncTaskEx<FetchListsTaskInput, Void, FetchListsTaskOutput> {

		@Override
		protected FetchListsTaskOutput doInBackground(FetchListsTaskInput... inputArray) {

			TwitterLists result = null;
			FetchListsTaskInput input = inputArray[0];
			Twitter twitter = getTwitterInstance();
			if (twitter != null) {
				try {
					if (input.mUserId != null) {
						ResponseList<UserList> lists = twitter.getAllUserLists(input.mUserId);
						result = new TwitterLists(lists);
					} else if (input.mScreenName != null) {
						ResponseList<UserList> lists = twitter.getAllUserLists(input.mScreenName);
						result = new TwitterLists(lists);
					}
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}

			return new FetchListsTaskOutput(input.mCallbackHandle, result);
		}

		@Override
		protected void onPostExecute(FetchListsTaskOutput output) {
			
			FinishedCallback callback = getFetchStatusesCallback(output.mCallbackHandle);
			if (callback != null) {
				callback.finished(true, output.mLists);
				removeFetchStatusesCallback(callback);
			}

			super.onPostExecute(output);
		}
	}

}