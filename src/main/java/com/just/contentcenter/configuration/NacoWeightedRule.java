package com.just.contentcenter.configuration;



import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.alibaba.nacos.ribbon.NacosServer;


/**
 * @author yanghao
 * @date 2020/1/20 14:53
 */
@Slf4j
public class NacoWeightedRule extends AbstractLoadBalancerRule {

    @Autowired
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        //读取配置文件，初始化NacoWeightedRule
    }

    @Override
    public Server choose(Object o) {
        try {
            BaseLoadBalancer loadBalancer = (BaseLoadBalancer)this.getLoadBalancer();
            //log.info("lb = {}",loadBalancer);
            //想要请求的微服务的名称
            String name = loadBalancer.getName();

            //拿到服务发现的API
            NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
            Instance instance = namingService.selectOneHealthyInstance(name);
            log.info("选择的实例是：port = {},instance ={}",instance.getPort(),instance);
            return new NacosServer(instance);
        } catch (NacosException e) {
            return null;
        }
    }
}
