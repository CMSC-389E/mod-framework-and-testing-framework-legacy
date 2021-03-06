package modblocks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import modblocks.CommandTest.TestRun;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandSubmit extends CommandBase {
	public static TestRun mostRecentTestRun = null;
	public static boolean waitingForInput = false;
	public static String uname;
	public static String pwd;
	
	public CommandSubmit() {
		System.out.println("this ran");
		try {
			System.setErr(new PrintStream("err.out"));
		} catch(FileNotFoundException e) {
			try {
				new File("err.out").createNewFile();
				System.setErr(new PrintStream("err.out"));
			} catch(IOException e1) {
				e1.printStackTrace();
			}
		}
		try {
			System.setOut(new PrintStream("feedback.out"));
		} catch(FileNotFoundException e) {
			try {
				new File("feedback.out").createNewFile();
			} catch(IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		
	}

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		File f = Paths.get("submit", "submit.txt").toFile();
//		File f = new File("submit\\submit.txt");
		if(!f.exists())
			sender.sendMessage(new TextComponentString("Submit file has not been configured yet.  Run \"/load <proj#> or follow instructions for local setup\""));
		else if(mostRecentTestRun == null)
			sender.sendMessage(new TextComponentString("The current test build has not been executed yet.  Run \"/test\""));
		else {
			// execute the upload here. Necessary to create a temp
			// directory with output results
			try {
				String worldname = sender.getEntityWorld().getWorldInfo().getWorldName();
				String part = (args.length == 1) ? args[0] : "";
				InputStream testFile = (new URL("http://www.cs.umd.edu/~abrassel/proj"+worldname.charAt(worldname.length()-1) +part +"tests.txt")).openStream();
				boolean valid = IOUtils.contentEquals(testFile, new ByteArrayInputStream(CommandTest.testFile));
				if (!valid) {
					throw new CommandException("You must revert your testing framework to the default project testing setup in order to submit your project.",new Object[0]);
				}
				
				FileInputStream in = new FileInputStream(Paths.get("submit", "submit.txt").toFile());
				byte[] mask = Files.readAllBytes(Paths.get("submit","submit.txt"));
				in.close();
				try {
					in = new FileInputStream(Paths.get("saves",worldname,".info").toFile());
				}
				catch (IOException e) {
					sender.sendMessage(new TextComponentString("Rename the save folder and world to Project#"));
				}
				byte[] users = Files.readAllBytes(Paths.get("saves",worldname,".info"));
				in.close();
				String name = null;
				if (uname == null) {
					try {
						File submitUser = Paths.get("submit",".submitUser").toFile();
						BufferedReader sUser = new BufferedReader(new FileReader(submitUser));
						name = sUser.readLine().split("=")[1];
						sUser.close();
					} catch(FileNotFoundException e) {
						throw new CommandException("Need to create submission profile.  Please enter the command \"/account <dID> <pwd>\"", new Object[0]);
					}
				}
				else {
					name = uname;
				}


				byte[] test = (mostRecentTestRun.testResults + "EOF\n" + (name != null ? name + "\nEOF\n" : "")).getBytes();
				byte[] message = new byte[test.length + users.length];
				byte[] correctWorld = ("\nWorld: " + sender.getEntityWorld().getWorldInfo().getWorldName()).getBytes();

				for(int i = 0; i < test.length; i++)
					message[i] = (byte)(test[i] ^ mask[i % mask.length]);
				for(int i = 0; i < users.length; i++)
					message[i + test.length] = (byte)(users[i] ^ mask[(i + test.length) % mask.length]);
				for(int i = 0; i < correctWorld.length; i++)
					message[i + test.length] = (byte)(users[i] ^ mask[(i + test.length) % mask.length]);

				File dummy = Paths.get("submit","hello.java").toFile();
				if(!dummy.exists())
					dummy.createNewFile();
				FileOutputStream file = new FileOutputStream(Paths.get("submit","results.txt").toFile());
				file.write(message);
				file.close();
				Process p = Runtime.getRuntime().exec("java -jar submit.jar", null, Paths.get(System.getProperty("user.dir"), "submit").toFile());
				/**
				 * CLEANUP SUBMIT
				 */
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							p.waitFor();
						} catch(InterruptedException e) {
							try {
								Files.delete(Paths.get("submit", "results.txt"));
							} catch(IOException e1) {
								System.out.println(e1.getMessage());
								sender.sendMessage(new TextComponentString("Problem with cleanup #2.  Aborting cleanup."));
							}
						}
						
						try {
							Files.delete(Paths.get("submit", "results.txt"));
						} catch(IOException e) {
							sender.sendMessage(new TextComponentString("Problem with cleanup #1.  Aborting cleanup."));
						}
						
					}
				});
				t.start();
				
				File submitUser = Paths.get("submit",".submitUser").toFile();
				boolean hasname = submitUser.exists();

				if(!hasname) {
					if(uname == null || pwd == null)
						throw new CommandException("Need to create submission profile.  Please enter the command \"/account <uid> <pwd>\"", new Object[0]);

					try {
						Thread.sleep(100);
					} catch(InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					OutputStreamWriter input = new OutputStreamWriter(p.getOutputStream());
					try {
						input.write(uname + "\n");
						input.flush();
					}
					catch (IOException e) {
						sender.sendMessage(new TextComponentString("detected missing submit.jar.  Downloading..."));
						
			    		InputStream loadsubmitjar = null;
						try {
							loadsubmitjar = new URL("http://www.cs.umd.edu/~abrassel/submit.jar").openStream();
				    		Files.copy(loadsubmitjar, Paths.get("submit","submit.jar"), StandardCopyOption.REPLACE_EXISTING);
						} catch(MalformedURLException z) {
							throw new CommandException("Tried to load an invalid submit.jar url: "+e.getMessage());
						} catch(IOException z) {
							throw new CommandException("Could not read remote submit.jar into local submit.jar");
						}
						finally {
							try {
								loadsubmitjar.close();
							} catch(IOException z) {
								e.printStackTrace();
							}
						}
						sender.sendMessage(new TextComponentString("Success! Restart the submission process."));
						return;
					}
					sender.sendMessage(new TextComponentString("got here."));
					try {
						Thread.sleep(1000);

					} catch(InterruptedException e) {
						e.printStackTrace();
					}

					input.write(pwd);
					input.flush();

					input.close();
				}
				boolean finished = p.waitFor(5, TimeUnit.SECONDS);

				if(!finished)
					throw new CommandException("Submission timed out for some reason.  Check submit server for submission.  If not there, run this command again.", new Object[0]);

				int result = p.exitValue();
				if(result == 1) {
					uname = null;
					pwd = null;
					Files.delete(Paths.get("submit",".submitUser"));
					throw new CommandException("Either account was invalid or .userProfile one time password expired.  Fix by running \"/account\"", new Object[0]);
				}

				sender.sendMessage(new TextComponentString("Submitted successfully"));

			} catch(IOException | InterruptedException e) {
				sender.sendMessage(new TextComponentString(Arrays.toString(e.getStackTrace())));
				sender.sendMessage(new TextComponentString(e.getMessage()));
				sender.sendMessage(new TextComponentString("Your project name and/or save folder must be named ProjectX, where X is the project num."));
			}
		}
	}

	public String getName() {
		return "submit";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "Submit the most recent test build";
	}
}
