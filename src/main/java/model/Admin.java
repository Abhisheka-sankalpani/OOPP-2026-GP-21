package model;



public class Admin extends User {
    public Admin(String userId, String passwordHash,
                 String firstName, String lastName,
                 String email, String contactNo,
                 String profilePic){
      super(userId,passwordHash,firstName,lastName,email,contactNo,profilePic,"ADMIN");
    }


    //get admin id
    public String getAdminId(){
      return super.getUserId();
    }
    @Override
    public String getRoleTitle() {
        return "System Administrator";
    }

    @Override
    public String getRoleSpecificDetails() {
        return "Full Administrative Access - Dean's Office / MIS Unit";
    }

    @Override
    public boolean hasAdminPrivileges() {
        return true;
    }

    public void displayAdminInfo(){
        System.out.println("Admin ID: " + getAdminId());
        System.out.println("Name: " + getFullname());
        System.out.println("Email: " + getEmail());
        System.out.println("Role: " + getRole());
    }
}
