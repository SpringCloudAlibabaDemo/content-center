package com.just.contentcenter.controller.content;

import com.just.contentcenter.domain.dto.content.ShareAuditDTO;
import com.just.contentcenter.domain.entity.content.Share;
import com.just.contentcenter.service.content.ShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shares")
public class ShareAdminController {

    @Autowired
    private ShareService shareService;

    @PutMapping("/audit/{id}")
    public Share auditById(@PathVariable Integer id, @RequestBody ShareAuditDTO shareAuditDTO){
        // TODO 认证、授权
        Share share = shareService.auditById(id, shareAuditDTO);
        return share;
    }
}
