package gitlet;

import static gitlet.Utils.message;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Zhurish
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS containsId
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            message("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                Repository.checkCommandLength(args, 1);
                Repository.init();
                break;
            case "add":
                Repository.checkCommandLength(args, 2);
                Repository.checkIfInitialized();
                Repository.add(args[1]);
                break;
            case "commit":
                Repository.checkCommandLength(args, 2);
                Repository.checkIfInitialized();
                Repository.commit(args[1]);
                break;
            case "rm":
                Repository.checkCommandLength(args, 2);
                Repository.checkIfInitialized();
                Repository.rm(args[1]);
                break;
            case "log":
                Repository.checkCommandLength(args, 1);
                Repository.checkIfInitialized();
                Repository.log();
                break;
            case "global-log":
                Repository.checkCommandLength(args, 1);
                Repository.checkIfInitialized();
                Repository.globalLog();
                break;
            case "find":
                Repository.checkCommandLength(args, 2);
                Repository.checkIfInitialized();
                Repository.find(args[1]);
                break;
            case "status":
                Repository.checkCommandLength(args, 1);
                Repository.checkIfInitialized();
                Repository.status();
                break;
            case "checkout":
                Repository.checkIfInitialized();
                switch(args.length) {
                    case 2:
                        Repository.checkoutBranch(args[1]);
                        break;
                    case 3:
                        if (!args[1].equals("--")) {
                            message("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkoutFile(args[2]);
                        break;
                    case 4:
                        if (!args[2].equals("--")) {
                            message("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkoutCommit(args[1], args[3]);
                        break;
                    default:
                        message("Incorrect operands.");
                        System.exit(0);
                }
                break;
            case "branch":
                Repository.checkCommandLength(args, 2);
                Repository.checkIfInitialized();
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                Repository.checkCommandLength(args, 2);
                Repository.checkIfInitialized();
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                Repository.checkCommandLength(args, 2);
                Repository.checkIfInitialized();
                Repository.reset(args[1]);
                break;
            case "merge":
                Repository.checkCommandLength(args, 2);
                Repository.checkIfInitialized();
                Repository.merge(args[1]);
                break;
            default:
                message("No command with that name exists.");
                System.exit(0);
        }
    }
}
