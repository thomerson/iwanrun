package com.iwantrun.core.service.application.domain;

import java.sql.Blob;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="biz_case_attachments")
public class CaseAttachments {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private int id ;
	
	@Column(name="case_id",nullable=false)
	private int caseId ;
	
	@Column(name="file_name")
	private String fileName ;
	
	@Column(name="file_path")
	private String filePath ;
	
	@Column(name="page_path")
	private String pagePath ;
	

	@Column(name="file_snapshot")
	private Blob fileSnapShot;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	

	public int getCaseId() {
		return caseId;
	}

	public void setCaseId(int caseId) {
		this.caseId = caseId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}	

	public String getPagePath() {
		return pagePath;
	}

	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}

	public Blob getFileSnapShot() {
		return fileSnapShot;
	}

	public void setFileSnapShot(Blob fileSnapShot) {
		this.fileSnapShot = fileSnapShot;
	}

}