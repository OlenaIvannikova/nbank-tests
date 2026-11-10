package api.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends BaseModel {

  private long id;
  private String username;
  private String password;
  private String name;
  private String role;
  private List<AccountResponse> accounts;
}
