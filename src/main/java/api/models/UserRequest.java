package api.models;

import common.configs.Config;
import common.configs.Property;
import common.generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest extends BaseModel {

  @GeneratingRule(regex = "^[A-Za-z0-9]{3,15}$")
  private String username;

  @GeneratingRule(regex = "^[A-Z]{3}[a-z]{4}[0-9]{3}[$%&]{2}$")
  private String password;

  @GeneratingRule(regex = "^USER$")
  private String role;

  public static UserRequest getAdmin() {
    return UserRequest.builder()
        .username(Config.getProperty(Property.ADMIN_LOGIN))
        .password(Config.getProperty(Property.ADMIN_PASSWORD))
        .build();
  }
}
