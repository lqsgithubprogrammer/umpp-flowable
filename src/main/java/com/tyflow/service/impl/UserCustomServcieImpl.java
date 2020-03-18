package com.tyflow.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tyflow.service.UserCustomService;

@Service("UserCustomService")
@Transactional
public class UserCustomServcieImpl implements UserCustomService{

	@Override
	public boolean synchoronousUsers() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
