package com.ssafy.ododocintellij.tracker.manager;

import com.intellij.openapi.project.Project;

import java.util.ArrayDeque;
import java.util.Queue;


public class ProjectProvider {

    private Queue<Project> projects = new ArrayDeque<>();

    private ProjectProvider() {}

    private static class ProjectProviderHolder {
        private static final ProjectProvider INSTANCE = new ProjectProvider();
    }

    public static ProjectProvider getInstance() {
        return ProjectProviderHolder.INSTANCE;
    }

    public Queue<Project> getProjects() {
        return projects;
    }

}
