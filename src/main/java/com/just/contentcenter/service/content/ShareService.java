package com.just.contentcenter.service.content;

import com.just.contentcenter.dao.content.RocketmqTransactionLogMapper;
import com.just.contentcenter.dao.content.ShareMapper;
import com.just.contentcenter.domain.dto.content.ShareAuditDTO;
import com.just.contentcenter.domain.dto.content.ShareDTO;
import com.just.contentcenter.domain.dto.messaging.UserAddBonusMsgDTO;
import com.just.contentcenter.domain.dto.user.UserDTO;
import com.just.contentcenter.domain.entity.content.RocketmqTransactionLog;
import com.just.contentcenter.domain.entity.content.Share;
import com.just.contentcenter.domain.enums.AuditStatusEnum;
import com.just.contentcenter.feignclient.UserCenterFeignClient;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author yanghao
 * @date 2020/1/19 14:00
 */
@Service
@Slf4j
public class ShareService {

    @Autowired(required = false)
    private ShareMapper shareMapper;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserCenterFeignClient userCenterFeignClient;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    public ShareDTO findById(Integer id) {
        Share share = shareMapper.selectByPrimaryKey(id);
        Integer userId = share.getUserId();
        /*List<ServiceInstance> instances = discoveryClient.getInstances("user-center");
        //所有用户中心的请求地址
        List<String> targetURLS = instances.stream()
                .map(instance -> instance.getUri().toString() + "/users/{id}")
                .collect(Collectors.toList());
        int i = ThreadLocalRandom.current().nextInt(targetURLS.size());
        log.info("请求地址:{}",targetURLS.get(i));
        UserDTO  userDTO = restTemplate.getForObject(
                targetURLS.get(i),
                UserDTO.class,userId
        );*/
        /*UserDTO  userDTO = restTemplate.getForObject(
                "http://user-center/users/{userId}",
                UserDTO.class,userId
        );*/
        UserDTO userDTO = userCenterFeignClient.findById(userId);

        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share, shareDTO);
        shareDTO.setWxNickname(userDTO.getWxNickname());

        return shareDTO;
    }

    public Share auditById(Integer id, ShareAuditDTO shareAuditDTO) {

        Share share = shareMapper.selectByPrimaryKey(id);

        //1查询share是否存在
        if (share == null) {
            throw new IllegalArgumentException("参数非法，该分享不存在");
        }
        if (!Objects.equals("NOT_YET", share.getAuditStatus())) {
            throw new IllegalArgumentException("参数非法，该分享已审核通过或者不通过");
        }

        //3如果pass，那么为发布人添加积分
        if (AuditStatusEnum.PASS.equals(shareAuditDTO.getAuditStatusEnum())) {
            //发送半消息
            String transactionId = UUID.randomUUID().toString();
            rocketMQTemplate.sendMessageInTransaction(
                    "tx-add-bonus-group",
                    "add-bonus",
                    MessageBuilder
                            .withPayload(
                                    UserAddBonusMsgDTO.builder()
                                            .userId(share.getUserId())
                                            .bonus(50)
                                            .build()
                            )
                            .setHeader(RocketMQHeaders.TRANSACTION_ID, transactionId)
                            .setHeader("share_id", id)
                            .build(),
                    shareAuditDTO
            );
        } else {
            this.auditById(id,shareAuditDTO);
        }

        //2审核资源


        /*rocketMQTemplate.convertAndSend("add-bonus",
                UserAddBonusMsgDTO.builder()
                        .userId(share.getUserId())
                        .bonus(50)
                        .build());
*/
        return share;
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdInDB(Integer id,ShareAuditDTO shareAuditDTO) {
        Share share = Share.builder()
                .id(id)
                .auditStatus(shareAuditDTO.getAuditStatusEnum().toString())
                .reason(shareAuditDTO.getReason())
                .build();
        shareMapper.updateByPrimaryKeySelective(share);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdWithRocketMqLog(Integer id,ShareAuditDTO shareAuditDTO,String transactionId){
        auditByIdInDB(id,shareAuditDTO);
        rocketmqTransactionLogMapper.insertSelective(
                RocketmqTransactionLog.builder()
                        .transactionId(transactionId)
                        .log("审核分享")
                        .build()
        );
    }
}
