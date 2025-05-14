package cats.effect

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, ThreadFactory}

import org.apache.commons.daemon.{Daemon, DaemonContext}

import internals.{IOContextShift, IOTimer}

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

trait IODaemon {
  def init(context: DaemonContext): SyncIO[Unit]
  def start: IO[Unit]
  def stop: IO[Unit]
  def destroy: SyncIO[Unit]
}

object IODaemon {
  class Instance(ofDaemon: IODaemon,
                 executionContext: DaemonContext => ExecutionContext = ioDaemonGlobal) extends Daemon {
    private var context: DaemonContext = _
    private implicit lazy val execContext = executionContext(context)
    implicit def contextShift: ContextShift[IO] = IOContextShift(execContext)
    implicit def defaultTimer: Timer[IO] = IOTimer(execContext)


    def init(context: DaemonContext): Unit = {
      this.context = context
    }

    def start(): Unit = ???

    def stop(): Unit = ???

    def destroy(): Unit = ???
  }

  def ioDaemonGlobal(context: DaemonContext): ExecutionContext = {
    val bound = math.max(2, Runtime.getRuntime().availableProcessors())
    val executor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(
      bound,
      new ThreadFactory {
        val ctr = new AtomicInteger(0)
        def newThread(r: Runnable): Thread = {
          val back = new Thread(r, s"iodaemon-compute-${ctr.getAndIncrement()}")
          back.setDaemon(true)
          back
        }
      }
    ))

    new ExecutionContext {
      def execute(runnable: Runnable): Unit = {
        executor.execute(() => {
            try {
              runnable.run()
            } catch {
              case NonFatal(t) => reportFailure(t)
              case t: Exception => context.getController.fail(t)
            }
          })
      }

      def reportFailure(cause: Throwable): Unit =
        executor.reportFailure(cause)
    }
  }

}

