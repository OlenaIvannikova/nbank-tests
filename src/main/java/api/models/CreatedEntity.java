package api.models;

import api.requests.skeleton.Endpoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatedEntity {
  public Endpoint endpoint;
  public Long id;
  public DeletionCredentials credentials;
}
