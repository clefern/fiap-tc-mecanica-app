package com.fiap.mecanica.domain.repository;

import com.fiap.mecanica.domain.model.Admin;

public interface AdminRepository extends BaseRepository {
  Admin save(Admin admin);
}
