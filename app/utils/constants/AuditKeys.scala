/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.constants

object AuditTypes {
  val COMPLETE_MEMBER_SEARCH = "CompleteMemberSearch"
}

object AuditTransactionTypes {
  val MEMBER_SEARCH_RESULTS = "member-search-results"
}

object AuditJourneyTypes {
  val DEFAULT_JOURNEY = "journey"
  val SEARCH_API_ERROR = "searchAPIError"
  val RETRIEVE_API_ERROR = "retrieveAPIError"
  val NO_MEMBER_MATCHED = "noMemberMatched"
  val MEMBER_MATCHED_NO_DATA = "memberMatchedNoData"
  val RESULTS_DISPLAYED = "resultsDisplayed"
}

object AuditResultTypes {
  val MATCH = "MATCH"
  val NO_MATCH = "NO MATCH"
}
