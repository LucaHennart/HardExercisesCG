# This code is assuming the contract from the input is respected
# It does not handle any answer for when there is no solution

require 'set'

STDOUT.sync = true # DO NOT REMOVE

LEFT = 'LEFT'.freeze
RIGHT = 'RIGHT'.freeze
NONE = 'NONE'.freeze

ELEVATOR_ACTION = 'ELEVATOR'.freeze
WAIT_ACTION = 'WAIT'.freeze
BLOCK_ACTION = 'BLOCK'.freeze

WAIT_COST = 1
ELEVATE_COST = 3
BLOCK_COST = 3

class Game
  attr_writer :start_node
  attr_reader :nb_additional_elevators
  class << self
    attr_reader :elevators, :block
  end

  @elevators = Set.new
  @block = Set.new

  def initialize
    @nb_floors, @width, @nb_rounds, @exit_floor, @exit_pos, @nb_total_clones,
        @nb_additional_elevators, @nb_elevators = gets.split(' ').collect(&:to_i)
    dump_inputs
    @graph = Graph.new
    @start_node = nil

    @nb_elevators.times do
      floor, pos = gets.split(' ').collect(&:to_i)
      self.class.elevators.add(self.class.elevator_key(floor, pos))
    end
  end

  def play
    checked = Set.new
    @start_node.cost = 0
    queue = [@start_node]
    return ELEVATOR_ACTION if exit?(@start_node)

    while queue.any?
      node = min_edge(queue)
      return next_move(node) if exit?(node)

      queue.delete(node)
      checked.add(node)
      node.next_round(@graph.nodes, @nb_floors, @width)
      neighbours = node.to_edges.reject { |edge| checked.include?(edge.to_node) }.collect(&:to_node)
      neighbours.each { |neighbour| update_nodes(node, neighbour) }
      queue.push(*neighbours)
    end
  end

  def reset
    @graph = Graph.new
  end

  def self.elevator?(floor, pos)
    elevators.include?(elevator_key(floor, pos))
  end

  def self.block?(floor, pos)
    block.include?(block_key(floor, pos))
  end

  def self.elevator_key(floor, pos)
    "#{floor};#{pos}"
  end

  def self.block_key(floor, pos)
    "#{floor};#{pos}"
  end

  private def min_edge(next_nodes)
    min_cost = Node::MAX_COST
    min_node = nil
    next_nodes.each do |node|
      if node.cost < min_cost
        min_cost = node.cost
        min_node = node
      end
    end
    STDERR.puts 'Nil node for :min_edge' if min_node.nil?
    min_node
  end

  private def update_nodes(from_node, to_node)
    action_cost = from_node.to_edges.select { |edge| edge.to_node == to_node }.first.cost
    to_node.cost = from_node.cost + action_cost if to_node.cost > from_node.cost + action_cost
  end

  private def next_move(node)
    parent = node.from_edge.from_node
    until parent.is_a?(InitialNode)
      node = parent
      parent = node.from_edge.from_node
    end
    move = parent.to_edges.select { |edge| edge.to_node == node }.first
    if move.is_a?(Elevate)
      elevator = move.from_node
      self.class.elevators.add(self.class.elevator_key(elevator.floor, elevator.pos))
      @nb_additional_elevators -= 1 unless move.to_node.nb_elevators == @nb_additional_elevators
    elsif move.is_a?(Block)
      block = move.from_node
      self.class.block.add(self.class.block_key(block.floor, block.pos))
    end
    move.to_s
  end

  private def exit?(node)
    node.floor == @exit_floor && node.pos == @exit_pos
  end

  private def dump_inputs
    STDERR.puts "Starting game with parameters nb_floors=#{@nb_floors}, width=#{@width}, " \
                "@nb_rounds=#{@nb_rounds}, @exit_floor=#{@exit_floor}, @exit_pos=#{@exit_pos}, " \
                "@nb_total_clones=#{@nb_total_clones}, " \
                "@nb_add_elevators=#{@nb_additional_elevators}, @nb_elevators=#{@nb_elevators}"
  end

end

class Graph
  attr_accessor :nodes
  attr_accessor :start_node

  def initialize
    @nodes = {}
  end
end

class Edge
  attr_accessor :from_node, :to_node
  def initialize(from, to)
    @from_node = from
    @to_node = to
  end

  def to_s; end

  def cost; end
end

class Wait < Edge
  def initialize(from, to)
    super(from, to)
  end

  def to_s
    WAIT_ACTION
  end

  def cost
    WAIT_COST
  end
end

class Block < Edge
  def initialize(from, to)
    super(from, to)
  end

  def to_s
    BLOCK_ACTION
  end

  def cost
    BLOCK_COST
  end
end

class Elevate < Edge
  def initialize(from, to)
    super(from, to)
  end

  def to_s
    ELEVATOR_ACTION
  end

  def cost
    ELEVATE_COST
  end
end

class Node
  attr_reader :floor, :pos, :direction, :nb_elevators
  attr_accessor :cost, :from_edge, :to_edges

  MAX_COST = 601

  def initialize(floor, pos, direction, nb_elevators)
    @floor = floor.to_i
    @pos = pos.to_i
    @direction = direction
    @nb_elevators = nb_elevators
    @cost = MAX_COST
    @from_edge = nil
    @to_edges = []
  end

  def next_round(nodes, nb_floors, width)
    wait_if_possible(nodes, width)
    elevate_if_possible(nodes, nb_floors)
    block_if_possible(nodes)
  end

  def to_s
    "(#{@floor};#{@pos});#{@direction};Remaining_elevators=#{@nb_elevators};Round=#{@cost}"
  end

  private def block_if_possible(nodes)
    direction = @direction == LEFT ? RIGHT : LEFT
    block(direction, nodes) if can_block?
  end

  private def can_block?
    !Game.block?(@floor, @pos) && !Game.elevator?(@floor, @pos)
  end

  private def wait_if_possible(nodes, width)
    if Game.elevator?(@floor, @pos)
      floor = @floor + 1
      pos = @pos
      direction = @direction
    elsif Game.block?(@floor, @pos) && !is_a?(InitialNode)
      floor = @floor
      direction = @direction == LEFT ? RIGHT : LEFT
      pos = direction == LEFT ? @pos - 1 : @pos + 1
    elsif Game.block?(@floor, @direction == LEFT ? @pos - 1 : @pos + 1)
      floor = @floor
      direction = @direction == LEFT ? RIGHT : LEFT
      pos = direction == LEFT ? @pos - 1 : @pos + 1
    else
      pos = @direction == LEFT ? @pos - 1 : @pos + 1
      floor = @floor
      direction = @direction
    end
    wait(floor, pos, direction, nodes) if can_wait?(pos, width)
  end

  private def elevate_if_possible(nodes, nb_floors)
    floor = @floor + 1
    elevate(floor, nodes) if can_elevate?(floor, nb_floors)
  end

  private def can_elevate?(floor, nb_floors)
    floor < nb_floors &&
      !Game.elevator?(@floor, @pos) && @nb_elevators > 0 &&
      !Game.block?(@floor, @pos)
  end

  private def wait(floor, pos, direction, nodes)
    key = key(floor, pos, direction, @nb_elevators)
    present = nodes.include?(key)
    node = register(floor, pos, direction, @nb_elevators, nodes)
    wait_edge = Wait.new(self, node)
    link_to_edge(node, wait_edge, nodes, present)
  end

  private def elevate(floor, nodes)
    nb_elevators = Game.elevator?(@floor, @pos) ? @nb_elevators : @nb_elevators - 1
    key = key(floor, @pos, @direction, nb_elevators)
    present = nodes.include?(key)
    node = register(floor, @pos, @direction, nb_elevators, nodes)
    elevate_edge = Elevate.new(self, node)
    link_to_edge(node, elevate_edge, nodes, present)
  end

  private def block(direction, nodes)
    key = key(@floor, @pos, direction, @nb_elevators)
    present = nodes.include?(key)
    node = register(@floor, @pos, direction, @nb_elevators, nodes)
    block_edge = Block.new(self, node)
    link_to_edge(node, block_edge, nodes, present)
  end

  private def register(floor, pos, direction, nb_elevators, nodes)
    key = key(floor, pos, direction, nb_elevators)
    return nodes[key] if nodes.include?(key)

    nodes[key] = Node.new(floor, pos, direction, nb_elevators)
  end

  private def link_to_edge(node_to, edge, nodes, present)
    key = key(node_to.floor, node_to.pos, node_to.direction, node_to.nb_elevators)
    if (present && nodes[key].cost > cost + edge.cost) || !present
      @to_edges << edge
      node_to.from_edge = edge
    end
  end

  private def can_wait?(pos, width)
    pos >= 0 && pos < width
  end

  private def key(floor, pos, direction, nb_elevators)
    "(#{floor};#{pos});#{direction};#{nb_elevators}"
  end
end

class InitialNode < Node
  def initialize(floor, pos, direction, nb_elevators)
    super(floor, pos, direction, nb_elevators)
  end

  def to_s
    'Initial ' + super
  end
end

class ExitNode < Node
  def initialize(floor, pos, direction, nb_elevators)
    super(floor, pos, direction, nb_elevators)
  end

  def to_s
    'Exit ' + super
  end
end

game = Game.new
loop do
  clone_floor, clone_pos, direction = gets.split(' ')
  STDERR.puts "Inputs clone_floor=#{clone_floor}, clone_pos=#{clone_pos}, direction=#{direction}"
  if direction == NONE
    puts WAIT_ACTION
    STDERR.puts 'Waiting...'
    next
  end
  game.start_node = InitialNode.new(clone_floor, clone_pos, direction, game.nb_additional_elevators)
  move = game.play
  STDERR.puts "I should #{move}"
  game.reset
  puts move
end