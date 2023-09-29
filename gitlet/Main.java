package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Gabriel and Umar
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        String firstArg = args[0];
        Repository repository = Repository.load();

        switch (firstArg) {
            case "init":
                if (validateNumInputs(1, args)) {
                    repository.init();
                }
                break;
            case "add":
                if (validateNumInputs(2, args)) {
                    String fileName = args[1];
                    repository.add(fileName);
                }
                break;
            case "commit":
                if (validateNumInputs(2, args)) {
                    String message = args[1];
                    repository.commit(message);
                }
                break;
            case "restore":
                if (args.length == 3 && args[1].equals("--")) {
                    repository.restore(null, args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    repository.restore(args[1], args[3]);
                } else {
                    System.out.println("Incorrect operands.");
                    return;
                }
                break;
            case "log":
                if (validateNumInputs(1, args)) {
                    repository.log();
                    return;
                }
                break;
            case "rm":
                if (validateNumInputs(2, args)) {
                    String fileName = args[1];
                    repository.remove(fileName);
                    return;
                }
                break;
            case "status":
                if (validateNumInputs(1, args)) {
                    repository.status();
                    return;
                }
                break;
            case "global-log":
                if (validateNumInputs(1, args)) {
                    repository.globalLog();
                    return;
                }
                break;
            case "branch":
                if (validateNumInputs(2 ,args)) {
                    String branchName = args[1];
                    repository.branch(branchName);
                }
                break;
            case "find":
                if (validateNumInputs(2, args)) {
                    String message = args[1];
                    repository.find(message);
                }
                break;
            case "switch":
                if (validateNumInputs(2, args)) {
                    String branchName = args[1];
                    repository.switchBranch(branchName);
                }
                break;
            case "reset":
                if (validateNumInputs(2, args)) {
                    String id = args[1];
                    repository.reset(id);
                }
                break;
            case "rm-branch":
                if (validateNumInputs(2, args)) {
                    String name = args[1];
                    repository.removeBranch(name);
                }
                break;

            // if the function does not exist:
            default:
                System.out.println("No command with that name exists.");
                repository.save();
        }
    }

    /**
     * The following method is made as an abstraction for checking if the git tools
     * are called on valid inputs.
     *
     * @return
     */
    public static boolean validateNumInputs(int numArgs, String[] args) {
        if (args.length != numArgs) {
            System.out.println("Incorrect Operands.");
            return false;
        }
        return true;
    }
}
