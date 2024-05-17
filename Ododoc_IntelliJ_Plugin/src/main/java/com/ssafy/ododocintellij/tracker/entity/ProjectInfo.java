package com.ssafy.ododocintellij.tracker.entity;

import com.intellij.psi.PsiFile;

public class ProjectInfo implements Cloneable{

    private PsiFile psiFile;
    private String hash;
    private String sourceCode;

    public ProjectInfo(PsiFile psiFile, String hash, String sourceCode) {
        this.psiFile = psiFile;
        this.hash = hash;
        this.sourceCode = sourceCode;
    }

    public PsiFile getPsiFile() {
        return psiFile;
    }

    public String getHash() {
        return hash;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
