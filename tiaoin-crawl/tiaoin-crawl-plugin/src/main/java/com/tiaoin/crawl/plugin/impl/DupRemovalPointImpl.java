package com.tiaoin.crawl.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.tiaoin.crawl.core.listener.SpiderListener;
import com.tiaoin.crawl.core.plugin.DupRemovalPoint;
import com.tiaoin.crawl.core.task.Task;
import com.tiaoin.crawl.core.xml.Site;
import com.tiaoin.crawl.plugin.duplicate.DocIDServer;

public class DupRemovalPointImpl implements DupRemovalPoint {

    private SpiderListener     listener;
    private final Object       mutex   = new Object();
    private DocIDServer        db      = null;
    private Collection<String> newUrls = null;
    private Site               site    = null;

    public void init(Site site) {
        this.site = site;
        //this.listener = listener;
        synchronized (mutex) {
            if (db == null) {
                db = new DocIDServer(site.getName());
//                listener.onInfo(Thread.currentThread(), null, "DocIDServer -> " + site.getName()
//                                                              + " initial success...");
            }
        }
    }

    public void destroy() {
        synchronized (mutex) {
            if (db != null) {
                db.close();
                db = null;
                listener.onInfo(Thread.currentThread(), null, "DocIDServer -> " + site.getName()
                                                              + " destroy success...");
            }
        }
    }

    public void context(Task task, Collection<String> newUrls) {
        this.newUrls = newUrls;
    }

    public Collection<Task> removeDuplicateTask(Collection<Task> tasks) {
        synchronized (mutex) {
            if (db == null)
                return null;
        }
        Collection<Task> validTasks = new ArrayList<Task>();
        for (String url : newUrls) {
            Task newTask = new Task(url, site, 10);
            int docId = db.getDocId(url);
            if (docId < 0) {
                docId = db.getNewDocID(url);
                validTasks.add(newTask);
            }
        }

        return validTasks;
    }

}
