package com.tiaoin.crawl.core.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

//import com.tiaoin.crawl.core.infra.SpiderIOC;
//import com.tiaoin.crawl.core.infra.SpiderIOCs;
import com.tiaoin.crawl.core.listener.SpiderListener;
import com.tiaoin.crawl.core.xml.Extension;
import com.tiaoin.crawl.core.xml.Extensions;
import com.tiaoin.crawl.core.xml.Impl;
import com.tiaoin.crawl.core.xml.Plugin;

/**
 * 插件管理
 * 
 * @author sky.yang
 * @version $Id: PluginManager.java, v 1.0 2013-5-28 下午4:24:00 sky.yang Exp $
 */
public class PluginManager implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;

    private Map<String, Collection<Impl>> impls = new HashMap<String, Collection<Impl>>();
//    private final SpiderIOC               ioc   = SpiderIOCs.create();

    /**
     * 只用于生成配置文件的时候用
     * @date 2013-1-2 下午07:13:11
     * @return
     */
    public Plugin createPlugin() {
        Plugin plugin = new Plugin();
        Extensions extensions = new Extensions();
        for (String point : ExtensionPoints.toArray()) {
            Extension e = new Extension();
            Impl impl = new Impl();
            impl.setSort("0");
            impl.setType(null);
            impl.setValue(ExtensionPoints.getPointImplClassName(point));
            e.getImpl().add(impl);
            e.setPoint(point);
            extensions.getExtension().add(e);
        }
        plugin.setExtensions(extensions);

        return plugin;
    }

    /**
     * 加载配置文件。很重要 
     * @date 2013-1-2 下午07:13:49
     * @param plugins
     * @param listener
     * @throws Exception
     */
    public void loadPluginConf(Collection<Plugin> plugins, SpiderListener listener)
                                                                                   throws Exception {
        if (plugins == null || plugins.isEmpty())
            listener.onInfo(Thread.currentThread(), null, "there is no plugins to load");

        Comparator<Impl> implComp = new Comparator<Impl>() {
            public int compare(Impl o1, Impl o2) {
                //注意这里是优先级，1 < 0 < -1 
                int sort1 = Integer.parseInt(o1.getSort());
                int sort2 = Integer.parseInt(o2.getSort());
                if (sort1 == sort2)
                    return 0;
                return sort1 > sort2 ? 1 : -1;
            }
        };

        for (Plugin plugin : plugins) {
            if (!"1".equals(plugin.getEnable())) {
                listener.onInfo(Thread.currentThread(), null, "skip plugin[" + plugin.getName()
                                                              + "] cause it is not enable");
                continue;
            }

            Collection<Extension> extensions = plugin.getExtensions().getExtension();
            if (extensions == null || extensions.isEmpty()) {
                listener.onInfo(Thread.currentThread(), null, "plugin[" + plugin.getName()
                                                              + "] has no extentions to load");
                continue;
            }

            listener.onInfo(Thread.currentThread(), null, "plugin info -> \n" + plugin);

            for (Extension ext : extensions) {
                String point = ext.getPoint();
                if (!ExtensionPoints.contains(point)) {
                    String err = "plugin[" + plugin.getName() + "] extension-point[" + point
                                 + "] not found please confim the point value must be in "
                                 + ExtensionPoints.string();
                    Exception e = new Exception(err);
                    listener.onError(Thread.currentThread(), null, err, e);
                    throw e;
                }

                List<Impl> impls = ext.getImpl();
                if (impls == null || impls.isEmpty()) {
                    listener.onInfo(Thread.currentThread(), null,
                        "skip plugin[" + plugin.getName() + "] extension-point[" + point
                                + "] cause it has no impls to load");
                    continue;
                }

                //按照排序
                Collections.sort(impls, implComp);

                //一个扩展点有多个实现类
                Collection<Impl> _impls = this.impls.get(point);
                if (_impls != null)
                    _impls.addAll(impls);
                else
                    this.impls.put(point, impls);

                listener.onInfo(Thread.currentThread(), null, "plugin[" + plugin.getName()
                                                              + "] extension-point[" + point
                                                              + "] loading ok ");
            }
        }
    }

    /**
     * 获取扩展点的所有实现类
     * @date 2013-1-2 下午07:14:14
     * @param <T>
     * @param name
     * @return
     */
    public <T> ExtensionPoint<T> getExtensionPoint(final String pointName) {
        if (!this.impls.containsKey(pointName))
            return null;

        final Collection<Impl> _impls = this.impls.get(pointName);
        return new ExtensionPoint<T>() {
            public Collection<T> getExtensions() {
                Collection<T> result = new ArrayList<T>();
                for (Impl impl : _impls) {
                    T t = null;
                    String type = impl.getType();
                    String value = impl.getValue();
                    if ("ioc".equals(type)) {
                        //t = ioc.createExtensionInstance(value);
                        t = (T) applicationContext.getBean(value);
                    } else {
                        try {
                            Class<T> cls = (Class<T>) Class.forName(value);
                            t = cls.newInstance();
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("Impl class -> " + value
                                                       + " of ExtensionPoint[" + pointName
                                                       + "] not found !", e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException("Impl class -> " + value
                                                       + " of ExtensionPoint[" + pointName
                                                       + "] instaniation fail !", e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("Impl class -> " + value
                                                       + " of ExtensionPoint[" + pointName
                                                       + "] illegal access !", e);
                        }
                    }

                    result.add(t);
                }

                return result;
            }
        };
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
