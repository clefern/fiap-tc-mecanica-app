package com.fiap.mecanica.domain.model.command;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResetPasswordCommandTest {

  @Test
  @DisplayName("Should build ResetPasswordCommand correctly")
  void shouldBuildCorrectly() {
    ResetPasswordCommand cmd =
        ResetPasswordCommand.builder().token("token123").newPassword("newPass").build();

    assertNotNull(cmd);
    assertEquals("token123", cmd.getToken());
    assertEquals("newPass", cmd.getNewPassword());
  }

  @Test
  @DisplayName("Should handle setters and toString")
  void shouldHandleSettersAndToString() {
    ResetPasswordCommand cmd = ResetPasswordCommand.builder().build();
    cmd.setToken("t");
    cmd.setNewPassword("p");

    assertEquals("t", cmd.getToken());
    assertEquals("p", cmd.getNewPassword());

    String str = cmd.toString();
    assertTrue(str.contains("t"));
    assertTrue(str.contains("p"));
  }

  @Test
  @DisplayName("Should handle equals and hashCode")
  void shouldHandleEqualsAndHashCode() {
    ResetPasswordCommand cmd1 =
        ResetPasswordCommand.builder().token("token").newPassword("pass").build();

    ResetPasswordCommand cmd2 =
        ResetPasswordCommand.builder().token("token").newPassword("pass").build();

    assertEquals(cmd1, cmd2);
    assertEquals(cmd1.hashCode(), cmd2.hashCode());

    assertNotEquals(cmd1, new Object());
  }
}
