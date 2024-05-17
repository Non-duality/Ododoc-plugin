package com.ssafy.ododocintellij;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.ssafy.ododocintellij.tracker.entity.ProjectInfo;
import com.ssafy.ododocintellij.tracker.manager.ProjectTracker;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DisposableService implements Disposable {

    private final Project project;
    private List<PsiFile> psiFiles;

    public DisposableService(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void dispose() {
        System.out.println("종료: " + project.getName());

        CompletableFuture.runAsync(() -> {
            getProjectFileList();
            processProjectFiles();
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public static DisposableService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, DisposableService.class);
    }

    private void getProjectFileList() {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, "java", scope);

        PsiManager psiManager = PsiManager.getInstance(project);

        psiFiles = files.stream()
                .map(psiManager::findFile)
                .collect(Collectors.toList());
    }

    private void processProjectFiles() {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }

            ProjectTracker projectTracker = ProjectTracker.getInstance();
            Map<String, ProjectInfo> beforeProjectStatus = projectTracker.getBeforeProjectStatus();
            Map<String, ProjectInfo> currentProjectStatus = projectTracker.getCurrentProjectStatus();

            String allBeforeProjectStatus = "";
            String allCurrentProjectStatus = "";

            for (PsiFile file : psiFiles) {
                if (beforeProjectStatus.containsKey(file.getName())) {
                    System.out.println("삭제: " + file.getName());
                    beforeProjectStatus.remove(file.getName());
                } else {
                    allBeforeProjectStatus += getEncrypt(file.getName());
                }

                if (currentProjectStatus.containsKey(file.getName())) {
                    currentProjectStatus.remove(file.getName());
                } else {
                    allCurrentProjectStatus += getEncrypt(file.getName());
                }
            }

            allBeforeProjectStatus = getEncrypt(allBeforeProjectStatus);
            allCurrentProjectStatus = getEncrypt(allCurrentProjectStatus);

//            projectTracker.setAllBeforeProjectStatus(allBeforeProjectStatus);
//            projectTracker.setAllCurrentProjectStatus(allCurrentProjectStatus);
        });
    }

    private String getEncrypt(String code) {
        String result = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(code.getBytes());
            byte[] hashCode = md.digest();

            StringBuffer sb = new StringBuffer();
            for (byte b : hashCode) {
                sb.append(String.format("%02x", b));
            }
            result = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
