package Server;

public enum Commands {

    DISCONNECT, USERLIST, ROOMLIST, ROOMJOIN, SETNAME, COMMANDLIST, CURRENTROOM;

    public String getCommand() {
        return "$" + this.toString();
    }

    public String getDescription() {
        switch (this) {
            case DISCONNECT:
                return this.getCommand() + " - Disconnect from Server";

            case USERLIST:
                return this.getCommand() + " - Sends a list of users in the current chatroom";

            case ROOMLIST:
                return this.getCommand() + " - Sends a list of available chatrooms";

            case ROOMJOIN:
                return this.getCommand() + " - Changes the current chatroom to the new one (syntax: $ROOMJOIN<!>[new room])";

            case SETNAME:
                return this.getCommand() + " - Changes the current username to the new one (syntax: $SETNAME<!>[new name])";

            case COMMANDLIST:
                return this.getCommand() + " - Prints out this list";

            case CURRENTROOM:
                return this.getCommand() + " - Current chatroom you are in";

            default:
                return "An unexpected error occured";
        }
    }


}
