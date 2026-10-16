package models;

import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest extends BaseModel {
    @GeneratingRule(regex = "^[A-Za-z]{1,5} [A-Za-z]{1,5}$")
    private String name;
}
