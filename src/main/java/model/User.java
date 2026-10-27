
    package model;

    public class User {

        //attributes-encapusation
        private String userId;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private String email;
        private String contactNo;
        private String profilePic;
        private String role;

        //constructor
        public User(String userId,
                    String passwordHash,
                    String firstName,
                    String lastName,
                    String email,
                    String contactNo,
                    String profilePic,
                    String role
        ) {
            this.userId = userId;
            this.passwordHash = passwordHash;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.contactNo = contactNo;
            this.profilePic = profilePic;
            this.role = role;
        }

        //Getters
        public String getUserId() {
            return userId;
        }
        public String getPasswordHash() {
            return passwordHash;
        }
        public String getFirstName() {
            return firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public String getEmail() {
            return email;
        }
        public String getContactNo() {
            return contactNo;
        }
        public String getProfilePic() {
            return profilePic;
        }
        public String getRole() {
            return role;
        }

        //setters
        public  void setPasswordHash(String passwordHash) {
            this.passwordHash = passwordHash;
        }
        public  void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public  void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public  void setEmail(String email) {
            this.email = email;
        }
        public  void setContactNo(String contactNo) {
            this.contactNo = contactNo;
        }
        public  void setProfilePic(String profilePic) {
            this.profilePic = profilePic;
        }
        public  void setRole(String role) {
            this.role = role;
        }
        //common method
        public String getFullname() {
            return firstName + " " + lastName;
        }
     }

