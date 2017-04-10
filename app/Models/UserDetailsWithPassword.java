package Models;

public class UserDetailsWithPassword extends UserDetails{
    public byte[] password;
    public UserDetailsWithPassword(String email, byte[] password, String name, String phone) {
        super(email, name, phone);
        this.password = password;
    }
}
