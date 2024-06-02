package com.example.demo.entity.task;

import com.example.demo.entity.user.User;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
@Entity
public class Task implements ITask {
    @Id
    @GeneratedValue
    private long id;

    @ManyToOne()
    private User user;

  private TaskType type;

  @ManyToOne()
  private TaskTemplate template;

  private Status status;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "task_id")
  private List<TaskInfo> infoList;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name ="task_id")
    private List<VirtualMachine> machines;

    public List<VirtualMachine> getMachines() {
        return machines;
    }

    public void setMachines(List<VirtualMachine> machines) {
        this.machines = machines;
    }

    @Autowired
    public Task(User user, TaskType type) {
        this.user = user;
        this.type = type;
    }
    public Task(User user, TaskTemplate template)
    {
        this.user = user;
        this.template = template;
    }
    private Task()
    {

    }



    public Long getUserID() {
        return user.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return id==task.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), user.getId());
    }



    public TaskType getType() {
        return type;
    }




    public long getId() {return id;}








    public List<TaskInfo> getInfoList() {
        return infoList;
    }

    public void setInfoList(List<TaskInfo> infoList) {
        this.infoList = infoList;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskTemplate getTemplate() {
        return template;
    }

    public void setTemplate(TaskTemplate template) {
        this.template = template;
    }
}
