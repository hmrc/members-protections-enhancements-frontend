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

package repositories

import play.api.libs.json.Format
import uk.gov.hmrc.mongo.cache.{DataKey, EntityCache, MongoCacheRepository}

import scala.concurrent.{ExecutionContext, Future}

//This trait exists purely to make line 13 lazy to circumvent Scala 2 -> Scala 3 issues with HMRC Mongo
trait EntityCacheWorkaround[CacheId, A] extends EntityCache[CacheId, A] {
  val cacheRepo: MongoCacheRepository[CacheId]
  val format: Format[A]

  private lazy implicit val f: Format[A] = format
  private val dataKey    = DataKey[A]("dataKey")

  override def putCache(cacheId: CacheId)(data: A)(implicit ec: ExecutionContext): Future[Unit] =
    cacheRepo
      .put[A](cacheId)(dataKey, data)
      .map(_ => ())

  override def getFromCache(cacheId: CacheId): Future[Option[A]] =
    cacheRepo.get[A](cacheId)(dataKey)
}
