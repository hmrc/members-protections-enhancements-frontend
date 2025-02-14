/*
 * Copyright 2025 HM Revenue & Customs
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

package viewmodels

import play.api.i18n.Messages

sealed trait DisplayMessage

object DisplayMessage {

  sealed trait InlineMessage extends DisplayMessage

  case class Message(key: String, args: List[Message]) extends InlineMessage {

    def toMessage(implicit messages: Messages): String =
      messages(key, args.map(_.toMessage): _*)
  }

  object Message {

    def apply(key: String, args: Message*): Message =
      Message(key, args.toList)
  }

  case class LinkMessage(content: Message, url: String, attrs: Map[String, String], hiddenText: Option[Message] = None)
    extends InlineMessage {

    def withAttr(key: String, value: String): LinkMessage =
      copy(attrs = attrs + (key -> value))
  }

  object LinkMessage {

    def apply(content: Message, url: String): LinkMessage =
      LinkMessage(content, url, Map(), None)

    def apply(content: Message, url: String, hiddenText: Message): LinkMessage =
      LinkMessage(content, url, Map(), Some(hiddenText))
  }

}
