/*
 * Ivory: A Hadoop toolkit for web-scale information retrieval
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package ivory.cascade.retrieval;

import ivory.smrf.retrieval.Accumulator;

/**
 * @author Lidan Wang
 */
public class CascadeAccumulator extends Accumulator{ 
  private static final long serialVersionUID = 4029090556043927100L;

  // Index into the kept docs array that will be passed through the cascade.
	public int index_into_keptDocs = -1;

	public CascadeAccumulator(int docno, float score) {
		super(docno, score);
	}
}
