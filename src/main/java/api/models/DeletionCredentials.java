package api.models;

import common.configs.Config;
import common.configs.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeletionCredentials {

    String username;
    String password;

    public static DeletionCredentials admin() {
        return new DeletionCredentials(
                Config.getProperty(Property.ADMIN_LOGIN),
                Config.getProperty(Property.ADMIN_PASSWORD)
        );
    }
}

