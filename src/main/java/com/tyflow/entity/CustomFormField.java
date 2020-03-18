package com.tyflow.entity;

import java.util.List;

import org.flowable.form.model.FormField;
import org.flowable.form.model.Option;

public class CustomFormField extends FormField{

	private static final long serialVersionUID = 1L;
	
	private List<Option> optionList;

	public List<Option> getOptionList() {
		return optionList;
	}

	public void setOptionList(List<Option> optionList) {
		this.optionList = optionList;
	}

	
}
