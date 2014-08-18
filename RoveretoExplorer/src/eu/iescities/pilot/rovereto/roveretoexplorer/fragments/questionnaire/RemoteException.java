/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.iescities.pilot.rovereto.roveretoexplorer.fragments.questionnaire;

/**
 * @author mirko perillo
 * 
 */
public class RemoteException extends Exception {

	private static final long serialVersionUID = 2405566426312845132L;

	public RemoteException() {
		super();
	}

	public RemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemoteException(String message) {
		super(message);
	}

	public RemoteException(Throwable cause) {
		super(cause);
	}

}
