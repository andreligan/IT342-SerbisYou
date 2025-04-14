import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ServiceProvider")
public class ServiceProviderEntity {
    // ... other fields ...

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "addressId", nullable = false)
    private AddressEntity address;

    // ... rest of the class ...
} 