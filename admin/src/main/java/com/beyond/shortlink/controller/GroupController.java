package com.beyond.shortlink.controller;

import com.beyond.shortlink.common.convention.result.Result;
import com.beyond.shortlink.common.convention.result.Results;
import com.beyond.shortlink.dao.entity.GroupDO;
import com.beyond.shortlink.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;


    @PostMapping("")
    public Result<Void> save(@RequestBody GroupDO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

}
