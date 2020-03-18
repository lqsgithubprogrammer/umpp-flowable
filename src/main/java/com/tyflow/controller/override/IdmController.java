package com.tyflow.controller.override;

import java.util.ArrayList;
import java.util.List;

import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.flowable.ui.common.model.GroupRepresentation;
import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.idm.service.GroupService;
import org.flowable.ui.idm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 覆盖 idm 本身的url
 * 
 * @author xwq
 *
 */
@RestController
@RequestMapping("/app")
public class IdmController {

	@Autowired
	private UserService userService;
	@Autowired
	private GroupService groupService;
	
	
	@RequestMapping(value = "/rest/editor-users", method = RequestMethod.GET)
    public ResultListDataRepresentation getUsers(@RequestParam(value = "filter", required = false) String filter) {
        List<? extends User> matchingUsers = userService.getUsers(filter, null, null);
        List<UserRepresentation> userRepresentations = new ArrayList<>(matchingUsers.size());
        for (User user : matchingUsers) {
            userRepresentations.add(new UserRepresentation(user));
        }
        return new ResultListDataRepresentation(userRepresentations);
    }
	
	@RequestMapping(value = "/rest/editor-groups", method = RequestMethod.GET)
    public ResultListDataRepresentation getGroups(@RequestParam(required = false, value = "filter") String filter) {
        List<GroupRepresentation> result = new ArrayList<>();
        List<Group> groups = groupService.getGroups(filter);
        for (Group group : groups) {
            result.add(new GroupRepresentation(group));
        }
        return new ResultListDataRepresentation(result);
    }



}
