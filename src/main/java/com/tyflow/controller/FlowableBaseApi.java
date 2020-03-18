package com.tyflow.controller;

import org.flowable.engine.FormService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.ui.modeler.service.FlowableFormService;
import org.flowable.ui.modeler.service.ModelRelationService;
import org.flowable.ui.modeler.service.ModelServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FlowableBaseApi {

	// ui
	@Autowired
	protected ModelServiceImpl modelService;
	@Autowired
	protected FlowableFormService uiFormService;
	@Autowired
	protected ModelRelationService modelRelationService;

	// engine
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected RuntimeService runtimeService;
	@Autowired
	protected TaskService taskService;
	@Autowired
	protected FormRepositoryService formRepositoryService;
	@Autowired
	protected FormService formService;
	@Autowired
	protected HistoryService historyService;

	// jackson
	@Autowired
	protected ObjectMapper objectMapper;
	
}
