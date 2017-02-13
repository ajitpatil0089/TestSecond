/**
 *
 */
package com.applifire.repositorymgt.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.RejectCommitException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.applifire.db.entity.repository.aws.AwsUserRepositoryInfoRepository;
import com.applifire.entity.aws.AwsProject;
import com.applifire.entity.aws.AwsUserRepositoryInfo;
import com.applifire.entity.rad.RadCodeRepository;
import com.applifire.radsetup.model.RadSetup;
import com.applifire.service.ProcessNameEnum;
import com.applifire.service.ProcessStatusEnum;
import com.applifire.web.ui.advice.RuntimeLoginInfoHelper;

/**
 * @author viral This class has all the implementation regarding Repository
 *         Management with the use of GIT.
 */
@Component(value = "gitRepositoryManagment")
public class GitRepositoryManagment implements RepositoryManagement {
	
	@Autowired
	com.applifire.service.AwsTaskService taskService;
	
//	@Autowired
//	private AwsProjectRepository awsProjectRepository;
	
	@Autowired
	private RadSetup radSetup;
	
	@Autowired
	private AwsUserRepositoryInfoRepository awsUserRepositoryInfoRepository;

	/**
	 *
	 */
	public GitRepositoryManagment() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * This method is use to create bare repository for the project
	 *
	 * @param bareRepoLocation
	 *            : location on which we need to create Bare Repository
	 * @author viral
	 * @throws GitAPIException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Override
	public void createBareRepository(final File bareRepoLocation) throws IllegalStateException, GitAPIException, IOException {
		System.out.println("Enter in createBareRepository");
		try {
			bareRepoLocation.delete();
			System.out.println("Enter in deleting exting branch");
			Git.init().setBare(true).setDirectory(bareRepoLocation).call();
			System.out.println("bare repo createed");
			final Repository repository = FileRepositoryBuilder.create(new File(bareRepoLocation.getAbsolutePath(), Constants.DOT_GIT));

			repository.close();
			System.out.println("bare repo close");
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/***
	 * This methods is use to create local repository for project
	 *
	 * @param localRepoLocation
	 *            : Location at which we need to create local repository
	 * @author viral
	 * @throws GitAPIException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	@Override
	public void createLocalRepository(final File localRepoLocation) throws IllegalStateException, GitAPIException, IOException {

		// localRepoLocation.delete();

		Git.init().setBare(false).setDirectory(localRepoLocation).call();

		final Repository repository = FileRepositoryBuilder.create(new File(localRepoLocation.getAbsolutePath(), Constants.DOT_GIT));

		com.applifire.fw.Constants.log.info("Created a new repository at " + repository.getDirectory());
		repository.close();
	}

	/***
	 * This method is use to add a files or folder in local repository
	 *
	 * @param localRepoLocation
	 *            : Location of Local Repository
	 * @param needToAdd
	 *            Files which needs to be added in Repository
	 * @author viral
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws NoFilepatternException
	 * @throws Exception
	 */
	@Override
	public void addFilesToLocalRepository(final String localRepoLocation, final File needToAdd) throws IOException, NoFilepatternException, GitAPIException {
		final Repository repository = openRepository(localRepoLocation + File.separator + Constants.DOT_GIT);
		final Git git = new Git(repository);
		git.add().addFilepattern(".").call();

	}

	private Repository openRepository(final String localRepoLocation) throws IOException {
		final FileRepositoryBuilder builder = new FileRepositoryBuilder();
		final Repository repository = builder.setGitDir(new File(localRepoLocation)).readEnvironment() // scan
				// environment
				// GIT_*
				// variables
				.findGitDir() // scan up the file system tree
				.build();

		return repository;

	}

	/**
	 * This method is use to commit data in specific branch
	 *
	 * @param localRepoLocation
	 *            : repository location in which we commit the data
	 * @param commitMsg
	 *            : comment for the commit
	 * @author viral
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws RejectCommitException
	 * @throws WrongRepositoryStateException
	 * @throws ConcurrentRefUpdateException
	 * @throws UnmergedPathsException
	 * @throws NoMessageException
	 * @throws NoHeadException
	 */
	@Override
	public void commitInLocalRepository(final String localRepoLocation, final String commitMsg) throws IOException, NoHeadException, NoMessageException, UnmergedPathsException,
	ConcurrentRefUpdateException, WrongRepositoryStateException, RejectCommitException, GitAPIException {
		final Repository repository = openRepository(localRepoLocation + File.separator + Constants.DOT_GIT);
		final Git git = new Git(repository);
		git.commit().setMessage(commitMsg).call();

	}

	/**
	 * This method is use to push all newly committed data to master branch
	 *
	 * @param currentRepoLocation
	 *            : current branch path
	 * @param masterRepoLocation
	 *            : master branch path
	 * @author viral
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws TransportException
	 * @throws InvalidRemoteException
	 *
	 * @Hint: git push --set-upstream /home/viral/test/c1/masterRepo/p1/ master
	 */

	@Override
	public void pushDataToMasterRepository(final File currentRepoLocation, final String masterRepoLocation, final RuntimeLoginInfoHelper runtimeLoginInfoHelper,
			final AwsUserRepositoryInfo awsUserRepositoryInfo) {
		try {
			final Repository repository = openRepository(currentRepoLocation.getAbsolutePath() + "/" + Constants.DOT_GIT);
			final Git git = new Git(repository);

			final StoredConfig config = repository.getConfig();
			config.setString("remote", "origin", "url", masterRepoLocation);
			config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
			config.setString("user", null, "name", runtimeLoginInfoHelper.getLoginId());
			// fetch = +refs/heads/*:refs/remotes/origin/*
			config.save();

			doPushOpration(git, runtimeLoginInfoHelper, awsUserRepositoryInfo);
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/****
	 * This method is used to delete the files from the given git repository
	 *
	 * @param currentRepoLocation
	 * @param masterRepoLocation
	 * @param comment
	 * @param runtimeLoginInfoHelper
	 * @param awsUserRepositoryInfo
	 * @throws IOException
	 *
	 * @Hint git rm -f
	 *       checkgit/src/main/java/checkgit/app/shared/EntityAudit.java git
	 *       commit -m "Removed folder from repository" git push origin master
	 */
	@Override
	public void deleteFilesFromRepository(final RuntimeLoginInfoHelper runtimeLoginInfoHelper,final ArrayList<String> filesTodelete,final AwsProject awsProject,final RadCodeRepository codeRepository) throws IOException
	{
		final String workspace = radSetup.getCustomersPath() + File.separator + runtimeLoginInfoHelper.getAppCreatorId() + File.separator + radSetup.getUserworkSpace() + File.separator
				+ runtimeLoginInfoHelper.getUserId() + File.separator + runtimeLoginInfoHelper.getProjectId() + File.separator;
		final File currentRepoLocation=new File(workspace);
		final AwsUserRepositoryInfo awsUserRepositoryInfo=awsUserRepositoryInfoRepository.findByUserIdAndRepoId(runtimeLoginInfoHelper.getUserId(),codeRepository.getRepositoryId());

		final Repository repository = openRepository(currentRepoLocation.getAbsolutePath() + "/" + Constants.DOT_GIT);
		final Git git = new Git(repository);
		final StoredConfig config = repository.getConfig();
		config.setString("remote", "origin", "url", codeRepository.getRepositoryUrl());
		config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
		config.setString("user", null, "name", runtimeLoginInfoHelper.getLoginId());
		// fetch = +refs/heads/*:refs/remotes/origin/*
		config.save();
		try {
			if(awsUserRepositoryInfo!=null){
				doDeleteOperation(git, awsUserRepositoryInfo.getUserName(), awsUserRepositoryInfo.getPassword(),filesTodelete, runtimeLoginInfoHelper);}
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * This method is use to get all data which are committed by another user
	 *
	 * @param currentRepoLocation
	 *            : current branch location
	 * @param masterRepoLocation
	 *            : master branch path
	 * @param userName
	 *            user name for authentication
	 * @param password
	 *            password for authentication
	 * @author viral
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws TransportException
	 * @throws NoHeadException
	 * @throws RefNotFoundException
	 * @throws CanceledException
	 * @throws InvalidRemoteException
	 * @throws DetachedHeadException
	 * @throws InvalidConfigurationException
	 * @throws WrongRepositoryStateException
	 * @throws URISyntaxException
	 * @Hint git pull /home/viral/test/c1/masterRepo/p1/
	 */
	@Override
	public void pullDataFromMasterRepository(final File currentRepoLocation, final String masterRepoLocation, final String userName, final String password,
			final RuntimeLoginInfoHelper runtimeLoginInfoHelper) throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException,
			InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException, TransportException, GitAPIException, URISyntaxException {
		final Repository repository = openRepository(currentRepoLocation.getAbsolutePath() + "/" + Constants.DOT_GIT);

		final StoredConfig config = repository.getConfig();
		config.setString("remote", "origin", "url", masterRepoLocation);
		config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
		config.setString("user", null, "name", runtimeLoginInfoHelper.getLoginId());
		// fetch = +refs/heads/*:refs/remotes/origin/*
		config.save();
		final Git git = new Git(repository);

		final PullCommand pull = git.pull();

		try {
			doPullOpration(pull, userName, password, runtimeLoginInfoHelper);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		// pull.setProgressMonitor(new TextProgressMonitor());

	}

	public void doPullOpration(final PullCommand pull, final String userName, final String password, final RuntimeLoginInfoHelper loginInfoHelper) throws Exception {
		try {
			taskService.addProcessTask(ProcessNameEnum.CHECKOUT_UPDATE, loginInfoHelper);
			taskService.updateProcessTask(ProcessNameEnum.CHECKOUT_UPDATE, ProcessStatusEnum.INPROCESS, loginInfoHelper);
			System.out.println("Git Pull Command Started");
			pull.setProgressMonitor(new TextProgressMonitor()).setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password)).call();
			System.out.println("Git Pull End");
			taskService.updateProcessTask(ProcessNameEnum.CHECKOUT_UPDATE, ProcessStatusEnum.DONE, loginInfoHelper);
		} catch (final Exception ex) {
			ex.printStackTrace();
			taskService.updateProcessTask(ProcessNameEnum.CHECKOUT_UPDATE, ProcessStatusEnum.ERROR, loginInfoHelper);
		}
	}

	public void doPushOpration(final Git git, final RuntimeLoginInfoHelper loginInfoHelper, final AwsUserRepositoryInfo awsUserRepositoryInfo) throws Exception {
		try {
			final AwsProject awsProject = loginInfoHelper.getAwsProject();
			final String mobilePath=radSetup.getCustomersPath() + "/" + awsProject.getAppCreatorId() + "/" + radSetup.getUserworkSpace() + "/" + loginInfoHelper.getUserId() + "/" + awsProject.getProjectId()
					+ "/" + awsProject.getProjectHiddenName()+MOBILE;
			final File filePath=new File(mobilePath);
			taskService.addProcessTask(ProcessNameEnum.CHECKIN_COMMIT, loginInfoHelper);
			taskService.updateProcessTask(ProcessNameEnum.CHECKIN_COMMIT, ProcessStatusEnum.INPROCESS, loginInfoHelper);
			final PullCommand pull = git.pull();
			try {
				pull.setProgressMonitor(new TextProgressMonitor())
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(awsUserRepositoryInfo.getUserName(), awsUserRepositoryInfo.getPassword())).call();
			} catch (final Exception e) {
			}
			if (awsUserRepositoryInfo.getAppSourceCode()) {
				git.add().addFilepattern(awsProject.getProjectHiddenName()).call();
				git.add().addFilepattern(awsProject.getProjectHiddenName()+DB).call();
				if (filePath.exists()) {
					git.add().addFilepattern(awsProject.getProjectHiddenName()+MOBILE).call();
				}
			}
			if (awsUserRepositoryInfo.getSchedulerSourceCode()) {
				git.add().addFilepattern(awsProject.getProjectHiddenName() + SCHEDULERAPP).call();
			}
			if (awsUserRepositoryInfo.getSearch()) {
				git.add().addFilepattern(awsProject.getProjectHiddenName() + SOLRAPP).call();
				git.add().addFilepattern(awsProject.getProjectHiddenName() + SOLR_HOME).call();
			}
			if (awsUserRepositoryInfo.getExternalLibs()) {
				git.add().addFilepattern("app" + LIBS).call();
			}
			final String userName = awsUserRepositoryInfo.getUserName();
			final String password = awsUserRepositoryInfo.getPassword();
			System.out.println("Git Commit Command Started");
			git.commit().setMessage(awsUserRepositoryInfo.getComment()).call();
			git.push().setProgressMonitor(new TextProgressMonitor()).setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password)).call();
			System.out.println("Git Commit Command End");
			taskService.updateProcessTask(ProcessNameEnum.CHECKIN_COMMIT, ProcessStatusEnum.DONE, loginInfoHelper);
		} catch (final Exception e) {
			e.printStackTrace();
			taskService.updateProcessTask(ProcessNameEnum.CHECKIN_COMMIT, ProcessStatusEnum.ERROR, loginInfoHelper);
		}
	}

	public void doDeleteOperation(final Git git, final String userName, final String password, final ArrayList<String> fileToDelete, final RuntimeLoginInfoHelper runtimeLoginInfoHelper) {
		try {
			final RmCommand fielPatterns = git.rm();
			for (final String fileList : fileToDelete) {
				fielPatterns.addFilepattern(fileList);
			}
			fielPatterns.call();
			git.commit().setMessage("File deleting").call();
			git.push().setProgressMonitor(new TextProgressMonitor()).setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password)).call();

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void main1(final String[] args) throws Exception {

		final String user1p1 = "/home/viral/applifire/customers/ba9dced8-453e-49a3-b627-d497a1f91b17/user/18D01ABF-F632-496A-B379-FC50EDEAB8C0/ILO8HPS3VYT5RWTZDFKW";

		// FileUtils.deleteDirectory(new File(masterRepoPath));

		// FileUtils.deleteDirectory(new File(user1p1));

		// FileUtils.deleteDirectory(new File(user2p1));

		final GitRepositoryManagment gitRepositoryManagment = new GitRepositoryManagment();
		/*
		 * System.out.println("Remove all Repo");
		 *
		 *
		 * gitRepositoryManagment.createBareRepository(new
		 * File(masterRepoPath)); System.out.println("Master Repo Created");
		 */

		new File(user1p1);

		// gitRepositoryManagment.createLocalRepository(user1Repo);
		System.out.println("User1 Repo Created");
		// File user2Repo = new File(user2p1);
		// gitRepositoryManagment.createLocalRepository(user2Repo);
		// System.out.println("User2 Repo Created");

		// File newFile = new File(user1p1 + "/tdddex1t12121Aa.txt");
		// newFile.createNewFile();

		// gitRepositoryManagment.pullDataFromMasterRepository(user2Repo,
		// masterRepoPath, "viral.patel69@gmail.com", "viral.patel69");

		gitRepositoryManagment.addFilesToLocalRepository(user1p1, null);

		gitRepositoryManagment.commitInLocalRepository(user1p1, "1st Commit by user1");

		// gitRepositoryManagment.pushDataToMasterRepository(user1Repo,
		// masterRepoPath, "viral.patel69@gmail.com", "viral.patel69", "dgdg",
		// null);

		// gitRepositoryManagment.pullDataFromMasterRepository(user2Repo,
		// masterRepoPath, "viral.patel69@gmail.com", "viral.patel69");

		System.out.println("Done");

	}

	public void sslFreeRequest() throws Exception {
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
			}
		} };

		// Install the all-trusting trust manager
		final SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		final HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(final String hostname, final SSLSession session) {
				return true;
			}
		};

		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}

	public static void testRepo() throws IOException, URISyntaxException, IllegalStateException, GitAPIException {
		try {
			final String repoName = "repo3";
			final File dir = new File("/home/viral/Test/viral", repoName);
			final Git git = Git.init().setBare(true).setDirectory(dir).call();
			final RemoteConfig config = new RemoteConfig(git.getRepository().getConfig(), Constants.DEFAULT_REMOTE_NAME);
			config.addURI(new URIish("https://127.0.0.1/Test/repos/" + repoName + "121"));
			config.update(git.getRepository().getConfig());
			git.getRepository().getConfig().save();

		} catch (final JGitInternalException e) {
			e.printStackTrace();
		} catch (final URISyntaxException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addAndCommitCodeInLocalBranch(final String baseProjectPath, final String custId, final String projectId, final String projectHiddenName, final String commitComment) throws Exception {
		// String masterBranch=radSetup.getCustomersPath() + File.separator +
		// custId + File.separator + "projectRepos" + File.separator +
		// projectId;
		final String workspace = baseProjectPath + File.separator + custId + File.separator + projectId + File.separator;
		/*
		 * String tempLocation="/home/viral/test/anotherRepo";
		 * FileUtils.deleteDirectory(new File(tempLocation));
		 */

		addFilesToLocalRepository(workspace, new File(workspace + File.separator + projectHiddenName + File.separator));
		commitInLocalRepository(workspace, commitComment);

		/*
		 * repositoryManagement = new GitRepositoryManagment();
		 * repositoryManagement.createLocalRepository(new File(tempLocation));
		 * repositoryManagement.pullDataFromMasterRepository(new
		 * File(tempLocation), masterBranch, "", "");
		 */
	}

	public static void main2(final String[] args) throws InvalidRemoteException, TransportException, GitAPIException, IOException {
		final String remoteRepo = "https://github.com/viralpatel69/algotest1.git";
		final CloneCommand cloneCommand = Git.cloneRepository();

		cloneCommand.setURI(remoteRepo);

		cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("user", "password"));
		final Repository repository = cloneCommand.getRepository();

		final Git git = new Git(repository);
		git.rm().addFilepattern("").call();
		final StoredConfig config = repository.getConfig();
		config.setString("remote", "origin", "url", remoteRepo);
		config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
		// fetch = +refs/heads/*:refs/remotes/origin/*
		config.save();

		git.push().setProgressMonitor(new TextProgressMonitor()).setCredentialsProvider(new UsernamePasswordCredentialsProvider("viral", "viral")).call();
	}

	public static void main(final String args[]) throws NoFilepatternException, GitAPIException, IOException

	{
		System.out.println("in main");
		System.out.println("in main again");

		System.out.println("in main for third time");
		final String remoteRepo = "https://github.com/applifireAlgo/shoppingtwo.git";
		final CloneCommand cloneCommand = Git.cloneRepository();

		cloneCommand.setURI(remoteRepo);

		cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("user", "password"));
		final Repository repository = cloneCommand.getRepository();

		final Git git = new Git(repository);
		git.rm().addFilepattern("/../src/main/java/com/app/shared/SystemInfo.java").call();
		final StoredConfig config = repository.getConfig();
		config.setString("remote", "origin", "url", remoteRepo);
		config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
		// fetch = +refs/heads/*:refs/remotes/origin/*
		config.save();

		git.push().setProgressMonitor(new TextProgressMonitor()).setCredentialsProvider(new UsernamePasswordCredentialsProvider("applifire@algorhythm.co.in", "algo.2015")).call();
	}

}
