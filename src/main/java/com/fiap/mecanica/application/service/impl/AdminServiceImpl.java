package com.fiap.mecanica.application.service.impl;

import com.fiap.mecanica.application.service.AdminService;
import com.fiap.mecanica.domain.model.Admin;
import com.fiap.mecanica.domain.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {

  private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
  private final AdminRepository adminRepository;

  public AdminServiceImpl(AdminRepository adminRepository) {
    this.adminRepository = adminRepository;
  }

  @Override
  @Transactional
  public Admin create(Admin admin) {
    logger.info("Creating new admin: {}", admin.getNome());
    return adminRepository.save(admin);
  }
}
