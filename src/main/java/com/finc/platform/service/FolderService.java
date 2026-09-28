package com.finc.platform.service;

import java.util.List;
import java.util.NoSuchElementException;

import com.finc.platform.entity.Department;
import com.finc.platform.entity.File;
import com.finc.platform.entity.Folder;
import com.finc.platform.entity.User;
import com.finc.platform.repository.DepartmentRepository;
import com.finc.platform.repository.FileRepository;
import com.finc.platform.repository.FolderRepository;
import com.finc.platform.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service

public class FolderService {

    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    public FolderService(FolderRepository folderRepository, FileRepository fileRepository,
                         UserRepository userRepository, DepartmentRepository departmentRepository) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional
    public Folder createRootFolder(String name, Long ownerId, Long departmentId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + ownerId));
        Department department = departmentId == null ? null
                : departmentRepository.findById(departmentId)
                .orElseThrow(() -> new NoSuchElementException("Department not found: " + departmentId));
        return createRootFolder(name, owner, department);
    }

    @Transactional
    public Folder createRootFolder(String name, User owner, Department department) {
        Folder folder = new Folder(name, "/" + name, owner, department, null);
        return folderRepository.save(folder);
    }

    @Transactional
    public Folder createSubFolder(Long parentFolderId, String name, User owner) {
        Folder parent = getOrThrow(parentFolderId);
        Folder child = new Folder(name, parent.getPath() + "/" + name, owner, parent.getDepartment(), parent);
        return folderRepository.save(child);
    }

    @Transactional
    public Folder rename(Long folderId, String name) {
        Folder folder = getOrThrow(folderId);
        folder.rename(name);
        return folder;
    }

    @Transactional
    public Folder move(Long folderId, Long targetFolderId) {
        Folder folder = getOrThrow(folderId);
        Folder target = getOrThrow(targetFolderId);
        if (isDescendant(target, folder)) {
            throw new IllegalArgumentException("Cannot move a folder into its own sub-folder");
        }
        folder.setParentFolder(target);
        folder.setPath(target.getPath() + "/" + folder.getName());
        return folder;
    }

    @Transactional(readOnly = true)
    public List<File> listContents(Long folderId) {
        return fileRepository.findByFolder_FolderIdAndIsDeletedFalse(folderId);
    }

    @Transactional
    public void delete(Long folderId) {
        folderRepository.deleteById(folderId);
    }

    @Transactional(readOnly = true)
    public Folder getOrThrow(Long folderId) {
        return folderRepository.findById(folderId)
                .orElseThrow(() -> new NoSuchElementException("Folder not found: " + folderId));
    }

    private boolean isDescendant(Folder candidate, Folder ancestor) {
        Folder current = candidate;
        while (current != null) {
            if (current.getFolderId().equals(ancestor.getFolderId())) {
                return true;
            }
            current = current.getParentFolder();
        }
        return false;
    }
}
