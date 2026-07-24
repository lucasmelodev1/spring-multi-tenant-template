package com.example.demo.tenant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.tenant.dto.CreateTenantRequest;

@Service
@Transactional
public class TenantService {
  @Autowired
  private TenantRepository tenantRepository;

  @Autowired
  private TenantUserRepository tenantUserRepository;

  @Autowired
  private AuthenticationManager authenticationManager;

  public void createTenant(CreateTenantRequest dto) {
  }
}
