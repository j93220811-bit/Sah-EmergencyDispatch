package hospital.model;
public class StaffUser extends User {
    public StaffUser(String username, UserRole role) { super(username, role); }
    @Override public boolean canManageResources() {
        return getRole() == UserRole.COORDINATOR || getRole() == UserRole.ADMINISTRATOR;
    }
}
