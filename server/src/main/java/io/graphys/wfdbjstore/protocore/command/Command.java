package io.graphys.wfdbjstore.protocore.command;

import io.graphys.wfdbjstore.engine.WfdbExecution;
import io.graphys.wfdbjstore.protocol.description.Description;
import io.graphys.wfdbjstore.protocol.exchange.CommandType;
import io.graphys.wfdbjstore.protocol.content.Content;
import io.graphys.wfdbjstore.protocore.report.Report;
import io.graphys.wfdbjstore.protocore.report.ReportConsumer;
import io.graphys.wfdbjstore.protocore.report.WriteReportException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public abstract sealed class Command<T extends Description, R extends Content>
        implements WfdbExecution
        permits MetadataConnectionCommand, RecordReadCommand, SignalConnectionCommand, ReadSignalFlowCommand {

    private static final Logger logger = LogManager.getLogger(Command.class);

    protected final T description;
    protected final CommandType commandType;
    private final Report<R> report;
    private boolean reportWritten = false;

    public Command(CommandType commandType, T description, ReportConsumer consumer) {
        this.commandType = commandType;
        this.description = description;
        this.report = new Report<>(commandType, consumer);
    }

    public CommandType getCommandType() {
        return commandType;
    }

    protected abstract void doAndWriteReport() throws Exception;

    protected void setReportWritten(boolean written) {
        this.reportWritten = written;
    }

    public Report<R> getReport() {
        return report;
    }

    @Override
    public final void execute() {
        if (reportWritten) {
            throw new IllegalStateException("This command had already executed!");
        }
        try {
            doAndWriteReport();
            report.getWriter().writeSuccessCompletion();
        } catch (Exception e) {
            try {
                logger.error("error", e);
                report.getWriter().writeFailedCompletion(e);
            } catch (WriteReportException wre) {
                throw new IllegalStateException("The report should be writable at this call");
            }
        } finally {
            reportWritten = true;
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getDescription(), getCommandType());
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof Command<?,?> other) {
            return this.getCommandType().equals(other.getCommandType())
                    && this.getDescription().equals(other.getDescription());
        }
        return false;
    }



    protected final void writeContent(R content) throws Exception {
        report.getWriter().writeNext(content);
    }

    public final T getDescription() {
        return description;
    }
}



































