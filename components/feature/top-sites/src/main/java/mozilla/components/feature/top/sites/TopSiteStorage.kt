/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.top.sites

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import mozilla.components.feature.top.sites.adapter.TopSiteAdapter
import mozilla.components.feature.top.sites.db.TopSiteDatabase
import mozilla.components.feature.top.sites.db.TopSiteEntity

/**
 * A storage implementation for organizing top sites.
 */
class TopSiteStorage(
    context: Context
) {
    internal var database: Lazy<TopSiteDatabase> = lazy { TopSiteDatabase.get(context) }

    /**
     * Adds a new [TopSite].
     *
     * @param title The title string.
     * @param url The URL string.
     * @param isDefault Whether or not the top site added should be a default top site. This is
     * used to identify top sites that are added by the application.
     * @param isPinned Whether or not the top site is pinned by the user. This helps the application
     * differentiate between user pinned top sites and top sites generated from frequently visited
     * sites.
     */
    fun addTopSite(title: String, url: String, isDefault: Boolean = false, isPinned: Boolean = true) {
        TopSiteEntity(
            title = title,
            url = url,
            isDefault = isDefault,
            isPinned = isPinned,
            createdAt = System.currentTimeMillis()
        ).also { entity ->
            entity.id = database.value.topSiteDao().insertTopSite(entity)
        }
    }

    /**
     * Returns a [LiveData] list of all the [TopSite] instances.
     */
    fun getTopSites(): LiveData<List<TopSite>> {
        return Transformations.map(
            database.value.topSiteDao().getTopSites()
        ) { list ->
            list.map { entity -> TopSiteAdapter(entity) }
        }
    }

    /**
     * Returns all [TopSite]s as a [DataSource.Factory].
     */
    fun getTopSitesPaged(): DataSource.Factory<Int, TopSite> = database.value
        .topSiteDao()
        .getTopSitesPaged()
        .map { entity -> TopSiteAdapter(entity) }

    /**
     * Removes the given [TopSite].
     */
    fun removeTopSite(site: TopSite) {
        val topSiteEntity = (site as TopSiteAdapter).entity
        database.value.topSiteDao().deleteTopSite(topSiteEntity)
    }
}
