package com.lhn.watchandlearn.api;

import android.app.Application;

import com.octo.android.robospice.Jackson2SpringAndroidSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.springandroid.json.jackson2.Jackson2ObjectPersisterFactory;

/**
 * Copyright (c) 2013 , Saritasa LLC (www.saritasa.com) . All rights reserved.
 *
 * @author Hien Ngo
 */
public class WALSpiceService extends Jackson2SpringAndroidSpiceService {

    @Override
    public CacheManager createCacheManager(final Application aApplication) throws CacheCreationException{
        CacheManager cacheManager = new CacheManager();

        Jackson2ObjectPersisterFactory jackson2ObjectPersisterFactory = new Jackson2ObjectPersisterFactory(aApplication);
        jackson2ObjectPersisterFactory.setAsyncSaveEnabled(true);
        cacheManager.addPersister(jackson2ObjectPersisterFactory);

        return cacheManager;
    }
}
