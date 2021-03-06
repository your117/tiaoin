package com.tiaoin.crawl.plugin.impl;

import com.tiaoin.crawl.common.utils.StringUtil;
import com.tiaoin.crawl.core.fetcher.FetchResult;
import com.tiaoin.crawl.core.listener.SpiderListener;
import com.tiaoin.crawl.core.plugin.FetchPoint;
import com.tiaoin.crawl.core.task.Task;
import com.tiaoin.crawl.core.xml.Site;
import com.tiaoin.crawl.plugin.util.PageFetcherImpl;
import com.tiaoin.crawl.plugin.util.SpiderConfig;

/**
 * 一个Host一个FetchPointImpl对象
 * @author weiwei l.weiwei@163.com
 * @date 2013-1-7 下午06:40:05
 */
public class FetchPointImpl implements FetchPoint {

    private SpiderListener listener = null;
    private Task           task     = null;

    public void init(Site site) {
        //this.listener = listener;
    }

    public void destroy() {
    }

    public void context(Task task) throws Exception {
        this.task = task;
    }

    public FetchResult fetch(FetchResult result) throws Exception {
        synchronized (this.task.site) {
            if (this.task.site.fetcher == null) {
                PageFetcherImpl fetcher = new PageFetcherImpl();
                SpiderConfig config = new SpiderConfig();
                config.setCharset(task.site.getCharset());
                String sdelay = task.site.getReqDelay();
                if (sdelay == null || sdelay.trim().length() == 0)
                    sdelay = "200";

                int delay = StringUtil.toSeconds(sdelay).intValue() * 1000;
                if (delay < 0)
                    delay = 200;

                config.setPolitenessDelay(delay);
                fetcher.setConfig(config);

                fetcher.init(this.task.site);
                this.task.site.fetcher = fetcher;
            }
            FetchResult fr = this.task.site.fetcher.fetch(task.url.replace(" ", "%20"));
            return fr;
        }
        //		return fetch();
    }

    //	private FetchResult fetch(){
    //		FetchResult fetchResult = new FetchResult();
    //		CrawlerConfiguration config = new CrawlerConfiguration(task.url);
    //		
    //		listener.onInfo(Thread.currentThread(), "crawling url: " + task.url);
    //
    //		Url urlToCrawl = new Url(config.beginUrl(), 0);
    //        Page page = config.downloader().get(urlToCrawl.link());
    //        if (page.getStatusCode() != Status.OK) {
    //        	listener.onError(Thread.currentThread(), "errorUrl->" + urlToCrawl.link(), new Exception(page.getStatusCode().name() + " link->" + urlToCrawl.link()));
    //        } else {
    //        	org.eweb4j.spiderman.fetcher.Page _page = new org.eweb4j.spiderman.fetcher.Page();
    //			_page.setContent(page.getContent());
    //			_page.setContentType("text/html");
    //			_page.setContentData(page.getContent().getBytes());
    //			_page.setCharset(page.getCharset());
    //			_page.setUrl(page.getUrl());
    //			fetchResult.setPage(_page);
    //			fetchResult.setFetchedUrl(page.getUrl());
    //			fetchResult.setStatusCode(page.getStatusCode().ordinal());
    //        }
    //
    //        for (String l : page.getLinks()) {
    //            String link = config.normalizer().normalize(l);
    //            final Url url = new Url(link, urlToCrawl.depth() + 1);
    //            //是否进入递归抓取，如果进入递归就需要控制深度
    //        }
    //        
    //        return fetchResult;
    //	}

}
