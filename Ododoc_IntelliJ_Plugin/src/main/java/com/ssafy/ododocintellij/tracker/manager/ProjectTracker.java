package com.ssafy.ododocintellij.tracker.manager;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.ssafy.ododocintellij.tracker.entity.ProjectInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectTracker {

    private List<PsiFile> psiFiles;
    private Map<String, ProjectInfo> beforeProjectStatus = new HashMap<>();
    private Map<String, ProjectInfo> currentProjectStatus = new HashMap<>();
    private String allBeforeProjectStatus;
    private String allCurrentProjectStatus;
    private ProjectTracker(){}

    private static class ProjectTrackerHolder {
        private static final ProjectTracker INSTANCE = new ProjectTracker();
    }

    public static ProjectTracker getInstance() {
        return ProjectTrackerHolder.INSTANCE;
    }

    // 프로젝트 생성시 프로젝트 파일 상태들을 해쉬 값으로 저장
    public void initHashStatus(Project project) {
        ApplicationManager.getApplication().runReadAction(() ->{
            String fileHash = "";
            getProjectFileList(project);
            beforeProjectStatus.clear();

            for(PsiFile file : psiFiles){
                String codeHash = getEncrypt(file.getText());
                beforeProjectStatus.put(file.getName(), new ProjectInfo(file, codeHash, file.getText()));
                fileHash += getEncrypt(file.getName());
            }
            allBeforeProjectStatus = getEncrypt(fileHash);
        });
    }

    // 지금 현재 프로젝트 파일 상태들을 해쉬 값으로 저장
    public void currentHashStatus(Project project) {
        ApplicationManager.getApplication().runReadAction(() -> {
            String fileHash = "";
            getProjectFileList(project);
            currentProjectStatus.clear();

            for(PsiFile file : psiFiles){
                String codeHash = getEncrypt(file.getText());
                currentProjectStatus.put(file.getName(), new ProjectInfo(file, codeHash, file.getText()));
                fileHash += getEncrypt(file.getName());
            }
            allCurrentProjectStatus = getEncrypt(fileHash);
        });
    }

    private void getProjectFileList(Project project){

        ApplicationManager.getApplication().runReadAction(() -> {
            GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
            Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(project, "java", scope);

            PsiManager psiManager = PsiManager.getInstance(project);

            psiFiles = files.stream()
                    .map(file -> psiManager.findFile(file))
                    .toList();
        });
    }

    // 해쉬로 암호화
    private String getEncrypt(String code) {
        String result = null;

        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(code.getBytes());
            byte[] hashCode = md.digest();

            StringBuffer sb = new StringBuffer();
            for(byte b : hashCode){
                sb.append(String.format("%02x", b));
            }
            result = sb.toString();

        }catch (NoSuchAlgorithmException e){
            throw  new RuntimeException(e);
        }

        return result;
    }

    public Map<String, ProjectInfo> getBeforeProjectStatus() {
        return beforeProjectStatus;
    }

    public Map<String, ProjectInfo> getCurrentProjectStatus() {
        return currentProjectStatus;
    }

    public String getAllBeforeProjectStatus() {
        return allBeforeProjectStatus;
    }

    public String getAllCurrentProjectStatus() {
        return allCurrentProjectStatus;
    }

    public void setBeforeProjectStatus(Map<String, ProjectInfo> beforeProjectStatus) {
        this.beforeProjectStatus = beforeProjectStatus;
    }
}
