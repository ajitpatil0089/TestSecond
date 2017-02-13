/**
 *
 */
package com.applifire.repositorymgt.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.RejectCommitException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnRemoteDelete;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import com.applifire.db.entity.repository.aws.AwsUserRepositoryInfoRepository;
import com.applifire.entity.aws.AwsProject;
import com.applifire.entity.aws.AwsUserRepositoryInfo;
import com.applifire.entity.rad.RadCodeRepository;
import com.applifire.radsetup.model.RadSetup;
import com.applifire.service.AwsTaskService;
import com.applifire.service.ProcessNameEnum;
import com.applifire.service.ProcessStatusEnum;
import com.applifire.web.ui.advice.RuntimeLoginInfoHelper;

/**
 * @author viral
 *
 */
@Component(value = "SVNRepositoryManagement")
public class SVNRepositoryManagement implements RepositoryManagement {
	
	@Autowired
	private AwsTaskService taskService;

	@Autowired
	private AwsUserRepositoryInfoRepository awsUserRepositoryInfoRepository;
	
	@Autowired
	private RadSetup radSetUp;

	/*
	 * (non-Javadoc)o
	 *
	 * @see
	 * com.buzzor.repositorymgt.service.RepositoryManagement#createBareRepository
	 * (java.io.File)
	 */
	@Override
	public void createBareRepository(final File bareRepoLocation) throws IllegalStateException, GitAPIException, IOException {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.buzzor.repositorymgt.service.RepositoryManagement#createLocalRepository
	 * (java.io.File)
	 */
	@Override
	public void createLocalRepository(final File localRepoLocation) throws IllegalStateException, GitAPIException, IOException {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.buzzor.repositorymgt.service.RepositoryManagement#
	 * addFilesToLocalRepository(java.lang.String, java.io.File)
	 */
	@Override
	public void addFilesToLocalRepository(final String localRepoLocation, final File needToAdd) throws IOException, NoFilepatternException, GitAPIException {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.buzzor.repositorymgt.service.RepositoryManagement#commitInLocalRepository
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public void commitInLocalRepository(final String localRepoLocation, final String commitMsg) throws IOException, NoHeadException, NoMessageException, UnmergedPathsException,
	ConcurrentRefUpdateException, WrongRepositoryStateException, RejectCommitException, GitAPIException {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.buzzor.repositorymgt.service.RepositoryManagement#
	 * pushDataToMasterRepository(java.io.File, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String,
	 * com.buzzor.web.ui.advice.RuntimeLoginInfoHelper)
	 */
	@Override
	public void pushDataToMasterRepository(final File currentRepoLocation, final String masterRepoLocation, final RuntimeLoginInfoHelper runtimeLoginInfoHelper,
			final AwsUserRepositoryInfo awsUserRepositoryInfo) {
		try {
			/*
			 * git.add().addFilepattern(".").call();
			 *
			 * RevCommit commitCommand =
			 * git.commit().setMessage(comment).call();
			 *
			 * System.out.println("pushing data start");
			 * git.push().setProgressMonitor(new
			 * TextProgressMonitor()).setCredentialsProvider(new
			 * UsernamePasswordCredentialsProvider(userName, password)).call();
			 * System.out.println("pushing data End");
			 */
			doPushOpration(currentRepoLocation, masterRepoLocation, runtimeLoginInfoHelper, awsUserRepositoryInfo);
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.buzzor.repositorymgt.service.RepositoryManagement#
	 * pullDataFromMasterRepository(java.io.File, java.lang.String,
	 * java.lang.String, java.lang.String,
	 * com.buzzor.web.ui.advice.RuntimeLoginInfoHelper)
	 */
	@Override
	public void pullDataFromMasterRepository(final File currentRepoLocation, final String masterRepoLocation, final String userName, final String password,
			final RuntimeLoginInfoHelper runtimeLoginInfoHelper) throws IOException, WrongRepositoryStateException, InvalidConfigurationException, DetachedHeadException,
			InvalidRemoteException, CanceledException, RefNotFoundException, NoHeadException, TransportException, GitAPIException, URISyntaxException {
		try {
			doPullOpration(currentRepoLocation, masterRepoLocation, userName, password, runtimeLoginInfoHelper);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.buzzor.repositorymgt.service.RepositoryManagement#
	 * addAndCommitCodeInLocalBranch(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void addAndCommitCodeInLocalBranch(final String baseProjectPath, final String custId, final String projectId, final String projectHiddenName, final String commitComment)
			throws Exception {
	}

	public void doPullOpration(final File currentRepoLocationl, final String masterRepoLocation, final String userName, final String password,
			final RuntimeLoginInfoHelper loginInfoHelper) throws Exception {
		try {
			taskService.addProcessTask(ProcessNameEnum.CHECKOUT_UPDATE, loginInfoHelper);
			taskService.updateProcessTask(ProcessNameEnum.CHECKOUT_UPDATE, ProcessStatusEnum.INPROCESS, loginInfoHelper);
			final SVNURL url = SVNURL.parseURIEncoded(masterRepoLocation);
			final SVNRepository repository = SVNRepositoryFactory.create(url);

			final ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password);
			repository.setAuthenticationManager(authManager);

			repository.checkPath("", -1);

			final SVNClientManager ourClientManager = SVNClientManager.newInstance();
			ourClientManager.setAuthenticationManager(authManager);
			final File f = new File(currentRepoLocationl.getAbsolutePath());

			// ourClientManager.getUpdateClient().doCheckout(url, dstPath,
			// pegRevision, revision, depth, allowUnversionedObstructions)

			final SVNUpdateClient uc = ourClientManager.getUpdateClient();
			final long latestRevision = repository.getLatestRevision();
			uc.doCheckout(url, f, SVNRevision.UNDEFINED, SVNRevision.parse("" + latestRevision), SVNDepth.UNKNOWN, false);

			com.applifire.fw.Constants.log.debug("Done commtting code");

			taskService.updateProcessTask(ProcessNameEnum.CHECKOUT_UPDATE, ProcessStatusEnum.DONE, loginInfoHelper);
		} catch (final Exception ex) {
			ex.printStackTrace();
			taskService.updateProcessTask(ProcessNameEnum.CHECKOUT_UPDATE, ProcessStatusEnum.ERROR, loginInfoHelper);
		}
	}

	private void setIgnoreProperty(final File dir, final SVNClientManager ourClientManager) throws SVNException {
		final String ignoreFileList = "*.war\n*.txt\n*.cf\n*.log\n.gitignore\n*.pdf\nbuild\nlogs";
		ourClientManager.getWCClient().doSetProperty(dir, SVNProperty.IGNORE, SVNPropertyValue.create(ignoreFileList), true, SVNDepth.INFINITY, null, null);
	}

	public void doPushOpration(final File currentRepoLocationl, final String masterRepoLocation, final RuntimeLoginInfoHelper loginInfoHelper,
			final AwsUserRepositoryInfo awsUserRepositoryInfo) throws Exception {
		try {
			final AwsProject awsProject = loginInfoHelper.getAwsProject();
			taskService.addProcessTask(ProcessNameEnum.CHECKIN_COMMIT, loginInfoHelper);
			taskService.updateProcessTask(ProcessNameEnum.CHECKIN_COMMIT, ProcessStatusEnum.INPROCESS, loginInfoHelper);

			final SVNURL url = SVNURL.parseURIEncoded(masterRepoLocation);
			final SVNRepository repository = SVNRepositoryFactory.create(url);

			final ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(awsUserRepositoryInfo.getUserName(), awsUserRepositoryInfo.getPassword());
			repository.setAuthenticationManager(authManager);

			final SVNClientManager ourClientManager = SVNClientManager.newInstance();
			ourClientManager.setAuthenticationManager(authManager);
			final File f = new File(currentRepoLocationl.getAbsolutePath());

			final File[] files = new File[10];
			int index = 0;
			if (awsUserRepositoryInfo.getAppSourceCode()) {
				final File projectFolder = new File(currentRepoLocationl.getAbsolutePath() + "/" + awsProject.getProjectHiddenName());
				if (projectFolder.exists()) {
					files[index++] = projectFolder;
				}
				final File projectDbFolder = new File(currentRepoLocationl.getAbsolutePath() + "/" + awsProject.getProjectHiddenName() + DB);
				if (projectDbFolder.exists()) {
					files[index++] = projectDbFolder;
				}
				final File projectMobileFolder = new File(currentRepoLocationl.getAbsolutePath() + "/" + awsProject.getProjectHiddenName() + MOBILE);
				if (projectMobileFolder.exists()) {
					files[index++] = projectMobileFolder;
				}
			}
			if (awsUserRepositoryInfo.getSchedulerSourceCode()) {
				final File projectSchedulerFolder = new File(currentRepoLocationl.getAbsolutePath() + "/" + awsProject.getProjectHiddenName() + SCHEDULERAPP);
				if (projectSchedulerFolder.exists()) {
					files[index++] = projectSchedulerFolder;
				}
			}
			if (awsUserRepositoryInfo.getSearch()) {
				final File projectSolrFolder = new File(currentRepoLocationl.getAbsolutePath() + "/" + awsProject.getProjectHiddenName() + SOLR_HOME);
				if (projectSolrFolder.exists()) {
					files[index++] = projectSolrFolder;
				}
				final File projectSolrAppFolder = new File(currentRepoLocationl.getAbsolutePath() + "/" + awsProject.getProjectHiddenName() + SOLRAPP);
				if (projectSolrAppFolder.exists()) {
					files[index++] = projectSolrAppFolder;
				}
			}
			if (awsUserRepositoryInfo.getExternalLibs()) {
				final File projectLibsFolder = new File(currentRepoLocationl.getAbsolutePath() + "/" + "app" + LIBS);
				if (projectLibsFolder.exists()) {
					files[index++] = projectLibsFolder;
				}
			}

			final File[] fileList = Arrays.copyOf(files, index);
			// ourClientManager.getUpdateClient().doCheckout(url, dstPath,
			// pegRevision, revision, depth, allowUnversionedObstructions)

			final SVNUpdateClient uc = ourClientManager.getUpdateClient();
			final long latestRevision = repository.getLatestRevision();

			// ourClientManager.getWCClient().doUnlock(files, true);
			uc.doCheckout(url, f, SVNRevision.UNDEFINED, SVNRevision.parse("" + latestRevision), SVNDepth.UNKNOWN, false);
			com.applifire.fw.Constants.log.debug("Adding All File In Repo");
			setIgnoreProperty(f, ourClientManager);
			ourClientManager.getWCClient().doAdd(f, true, true, true, SVNDepth.INFINITY, true, true);
			// ourClientManager.getWCClient().doAdd(f, true, true, true, true);
			com.applifire.fw.Constants.log.debug("Adding OP Done");

			com.applifire.fw.Constants.log.debug("Getting All Files which need to be commit");
			final SVNCommitPacket[] packets = ourClientManager.getCommitClient().doCollectCommitItems(fileList, false, true, SVNDepth.INFINITY, false, new String[0]);
			System.out.println("Got All File which need to be Commit" + packets);
			new SVNProperties();
			com.applifire.fw.Constants.log.debug("Start Commit");

			final SVNCommitInfo[] commitResults = ourClientManager.getCommitClient().doCommit(packets, false, awsUserRepositoryInfo.getComment());
			com.applifire.fw.Constants.log.debug("Done" + commitResults);

			taskService.updateProcessTask(ProcessNameEnum.CHECKIN_COMMIT, ProcessStatusEnum.DONE, loginInfoHelper);
		} catch (final Exception e) {
			e.printStackTrace();
			taskService.updateProcessTask(ProcessNameEnum.CHECKIN_COMMIT, ProcessStatusEnum.ERROR, loginInfoHelper);
		}
	}

	@Override
	public void deleteFilesFromRepository(final RuntimeLoginInfoHelper runtimeLoginInfoHelper, final ArrayList<String> filesTodelete, final AwsProject awsProject,
			final RadCodeRepository codeRepository) throws IOException {
		final String workspace = radSetUp.getCustomersPath() + File.separator + runtimeLoginInfoHelper.getAppCreatorId() + File.separator + radSetUp.getUserworkSpace()
				+ File.separator + runtimeLoginInfoHelper.getUserId() + File.separator + runtimeLoginInfoHelper.getProjectId() + File.separator;
		final File currentRepoLocation = new File(workspace);
		try {
			final SVNURL url = SVNURL.parseURIEncoded(codeRepository.getRepositoryUrl());
			final SVNRepository repository = SVNRepositoryFactory.create(url);

			final AwsUserRepositoryInfo awsUserRepositoryInfo = awsUserRepositoryInfoRepository.findByUserIdAndRepoId(runtimeLoginInfoHelper.getUserId(),
					codeRepository.getRepositoryId());

			final ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(awsUserRepositoryInfo.getUserName(), awsUserRepositoryInfo.getPassword());
			repository.setAuthenticationManager(authManager);

			final SVNClientManager ourClientManager = SVNClientManager.newInstance();
			ourClientManager.setAuthenticationManager(authManager);
			new File(currentRepoLocation.getAbsolutePath());

			for (final String files : filesTodelete) {
				try {
					final SVNURL urls = SVNURL.parseURIDecoded(codeRepository.getRepositoryUrl() + "/" + files);
					doDelete(urls, authManager);
					com.applifire.fw.Constants.log.debug("Deleted file" + codeRepository.getRepositoryUrl() + "/" + files);
				} catch (final Exception e) {

				}

			}
		} catch (final Exception e) {
			com.applifire.fw.Constants.log.debug("Deleted file" + e.getMessage());
		}

	}

	public void doDelete(final SVNURL fileUrl, final ISVNAuthenticationManager authenticationManager) throws SVNException {
		final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		svnOperationFactory.setAuthenticationManager(authenticationManager);
		try {
			final SvnRemoteDelete remoteDelete = svnOperationFactory.createRemoteDelete();

			remoteDelete.setSingleTarget(SvnTarget.fromURL(fileUrl));
			remoteDelete.setCommitMessage("Delete a file from the repository");
			final SVNCommitInfo commitInfo = remoteDelete.run();
			if (commitInfo != null) {
				final long newRevision = commitInfo.getNewRevision();
				System.out.println("Removed a file, revision " + newRevision + " created");
			}
		} finally {
			svnOperationFactory.dispose();
		}
	}

}
